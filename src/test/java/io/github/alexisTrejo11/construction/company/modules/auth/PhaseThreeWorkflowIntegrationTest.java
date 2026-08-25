package io.github.alexisTrejo11.construction.company.modules.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.User;
import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.UserRole;
import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.UserStatus;
import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.Invitation;
import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.InvitationStatus;
import io.github.alexisTrejo11.construction.company.modules.user.shared.persistence.UserRepository;
import io.github.alexisTrejo11.construction.company.modules.user.shared.persistence.InvitationRepository;
import io.github.alexisTrejo11.construction.company.modules.project.shared.persistence.ProjectRepository;
import io.github.alexisTrejo11.construction.company.modules.project.members.shared.persistence.ProjectMemberRepository;
import java.util.EnumSet;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PhaseThreeWorkflowIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JavaMailSender mailSender;

    private User admin;

    @BeforeEach
    void setUp() {
        invitationRepository.deleteAll();
        projectMemberRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        admin = new User();
        admin.setEmail("admin@example.com");
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setPasswordHash(passwordEncoder.encode("a-secure-password"));
        admin.setStatus(UserStatus.ACTIVE);
        admin.setRoles(EnumSet.of(UserRole.COMPANY_ADMIN));
        admin = userRepository.save(admin);
    }

    @Test
    void csrfEndpointIssuesTokenAndProtectedRouteRejectsMissingToken() throws Exception {
        mockMvc.perform(get("/v2/api/auth/csrf"))
            .andExpect(status().isNoContent())
            .andExpect(header().exists("Set-Cookie"))
            .andExpect(header().exists("X-Trace-Id"));

        mockMvc.perform(patch("/v2/api/users/{userId}", admin.getId())
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Updated\",\"lastName\":\"Admin\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error.error_type").value("FORBIDDEN"))
            .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void activeUserCanLoginReuseSessionAndLogout() throws Exception {
        MvcResult login = mockMvc.perform(post("/v2/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@example.com\",\"password\":\"a-secure-password\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.email").value("admin@example.com"))
            .andReturn();

        Cookie session = login.getResponse().getCookie("SESSION");
        assertThat(session).isNotNull();

        mockMvc.perform(get("/v2/api/auth/me").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.email").value("admin@example.com"));

        mockMvc.perform(post("/v2/api/auth/logout").cookie(session).with(csrf()))
            .andExpect(status().isNoContent())
            .andExpect(result -> assertThat(result.getResponse().getContentAsString()).isEmpty());

        mockMvc.perform(get("/v2/api/auth/me").cookie(session))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.error_type").value("UNAUTHENTICATED"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void activeUserCanLoginUsingCsrfCookieAndHeader() throws Exception {
        MvcResult csrf = mockMvc.perform(get("/v2/api/auth/csrf"))
            .andExpect(status().isNoContent())
            .andReturn();

        Cookie csrfCookie = csrf.getResponse().getCookie("XSRF-TOKEN");
        assertThat(csrfCookie).isNotNull();

        mockMvc.perform(post("/v2/api/auth/login")
                .cookie(csrfCookie)
                .header("X-XSRF-TOKEN", csrfCookie.getValue())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@example.com\",\"password\":\"a-secure-password\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.email").value("admin@example.com"));
    }

    @Test
    void administrativeProfileUpdatePersistsAndValidatesRequest() throws Exception {
        mockMvc.perform(patch("/v2/api/users/{userId}", admin.getId())
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Updated\",\"lastName\":\"Admin\",\"phone\":\"123456\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.firstName").value("Updated"));

        assertThat(userRepository.findById(admin.getId()).orElseThrow().getPhone()).isEqualTo("123456");

        mockMvc.perform(patch("/v2/api/users/{userId}", admin.getId())
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"\",\"lastName\":\"Admin\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.error_type").value("VALIDATION"));

        mockMvc.perform(patch("/v2/api/users/{userId}", Long.MAX_VALUE)
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Updated\",\"lastName\":\"Admin\"}"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error.error_type").value("NOT_FOUND"));
    }

    @Test
    void invitationAcceptanceActivatesAccountAndCannotBeReused() throws Exception {
        mockMvc.perform(post("/v2/api/invitations")
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"invitee@example.com\",\"roles\":[\"SITE_ENGINEER\"]}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data").doesNotExist());

        ArgumentCaptor<SimpleMailMessage> message = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(message.capture());
        String token = message.getValue().getText().replace("Invitation token: ", "");

        mockMvc.perform(post("/v2/api/invitations/{token}/accept", token)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\":\"a-secure-password\",\"firstName\":\"Invited\",\"lastName\":\"User\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        User invitee = userRepository.findByEmail("invitee@example.com").orElseThrow();
        assertThat(passwordEncoder.matches("a-secure-password", invitee.getPasswordHash())).isTrue();

        mockMvc.perform(post("/v2/api/invitations/{token}/accept", token)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\":\"a-secure-password\",\"firstName\":\"Invited\",\"lastName\":\"User\"}"))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.error.error_type").value("BUSINESS_RULE"));
    }

    @Test
    void suspendingUserInvalidatesPersistedSessions() throws Exception {
        MvcResult login = mockMvc.perform(post("/v2/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@example.com\",\"password\":\"a-secure-password\"}"))
            .andExpect(status().isOk())
            .andReturn();

        Cookie session = login.getResponse().getCookie("SESSION");
        assertThat(session).isNotNull();

        mockMvc.perform(patch("/v2/api/users/{userId}/status", admin.getId())
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"SUSPENDED\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("SUSPENDED"));

        mockMvc.perform(get("/v2/api/auth/me").cookie(session))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void currentUserAndAdministrativeReadEndpointsReturnProfilesAndNotFoundOutcomes() throws Exception {
        mockMvc.perform(get("/v2/api/users/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.error_type").value("UNAUTHENTICATED"));

        mockMvc.perform(get("/v2/api/users/me")
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(admin.getId()));

        mockMvc.perform(patch("/v2/api/users/me")
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Current\",\"lastName\":\"User\",\"phone\":\"555\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.firstName").value("Current"));

        mockMvc.perform(patch("/v2/api/users/me")
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"\",\"lastName\":\"User\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.error_type").value("VALIDATION"));

        mockMvc.perform(get("/v2/api/users")
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].email").value(admin.getEmail()));

        mockMvc.perform(get("/v2/api/users/{userId}", admin.getId())
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.email").value(admin.getEmail()));

        mockMvc.perform(get("/v2/api/users/{userId}", Long.MAX_VALUE)
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN")))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error.error_type").value("NOT_FOUND"));
    }

    @Test
    void roleAndStatusEndpointsValidateAndRejectInvalidTargets() throws Exception {
        User target = new User();
        target.setEmail("target@example.com");
        target.setRoles(EnumSet.of(UserRole.SITE_ENGINEER));
        target = userRepository.save(target);

        mockMvc.perform(patch("/v2/api/users/{userId}/roles", target.getId())
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roles\":[\"PROJECT_MANAGER\"]}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.roles[0]").value("PROJECT_MANAGER"));

        mockMvc.perform(patch("/v2/api/users/{userId}/roles", target.getId())
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roles\":[]}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.error_type").value("VALIDATION"));

        mockMvc.perform(patch("/v2/api/users/{userId}/roles", Long.MAX_VALUE)
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roles\":[\"PROJECT_MANAGER\"]}"))
            .andExpect(status().isNotFound());

        User invited = new User();
        invited.setEmail("invited@example.com");
        invited.setRoles(EnumSet.of(UserRole.SITE_ENGINEER));
        invited = userRepository.save(invited);

        mockMvc.perform(patch("/v2/api/users/{userId}/status", invited.getId())
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"ACTIVE\"}"))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.error.error_type").value("BUSINESS_RULE"));

        admin.setStatus(UserStatus.DISABLED);
        userRepository.save(admin);

        mockMvc.perform(patch("/v2/api/users/{userId}/status", admin.getId())
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"ACTIVE\"}"))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.error.error_type").value("BUSINESS_RULE"));

        mockMvc.perform(patch("/v2/api/users/{userId}/status", Long.MAX_VALUE)
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"SUSPENDED\"}"))
            .andExpect(status().isNotFound());
    }

    @Test
    void authenticationRejectsInvalidCredentialsAndInactiveAccounts() throws Exception {
        mockMvc.perform(post("/v2/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@example.com\",\"password\":\"wrong-password\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.error_type").value("UNAUTHENTICATED"));

        for (UserStatus status : EnumSet.of(
            UserStatus.INVITED,
            UserStatus.SUSPENDED,
            UserStatus.DISABLED
        )) {
            User account = new User();
            account.setEmail(status.name().toLowerCase() + "@example.com");
            account.setFirstName("Inactive");
            account.setLastName("Account");
            account.setPasswordHash(passwordEncoder.encode("a-secure-password"));
            account.setStatus(status);
            account.setRoles(EnumSet.of(UserRole.SITE_ENGINEER));
            userRepository.save(account);

            mockMvc.perform(post("/v2/api/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"" + account.getEmail() + "\",\"password\":\"a-secure-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.error_type").value("UNAUTHENTICATED"));
        }
    }

    @Test
    void invitationEndpointsValidateInputAndRejectExpiredOrUnknownTokens() throws Exception {
        mockMvc.perform(post("/v2/api/invitations")
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"\",\"roles\":[]}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.error_type").value("VALIDATION"));

        mockMvc.perform(post("/v2/api/invitations/unknown/accept")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\":\"a-secure-password\",\"firstName\":\"Invited\",\"lastName\":\"User\"}"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error.error_type").value("NOT_FOUND"));

        mockMvc.perform(post("/v2/api/invitations/unknown/accept")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\":\"short\",\"firstName\":\"Invited\",\"lastName\":\"User\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.error_type").value("VALIDATION"));

        User invited = new User();
        invited.setEmail("expired@example.com");
        invited.setRoles(EnumSet.of(UserRole.SITE_ENGINEER));
        invited = userRepository.save(invited);

        Invitation invitation = new Invitation();
        invitation.setUser(invited);
        invitation.setTokenHash(hash("expired-token"));
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(Instant.now().minusSeconds(1));
        invitationRepository.save(invitation);

        mockMvc.perform(post("/v2/api/invitations/expired-token/accept")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\":\"a-secure-password\",\"firstName\":\"Invited\",\"lastName\":\"User\"}"))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.error.error_type").value("BUSINESS_RULE"));

        assertThat(invitationRepository.findById(invitation.getId()).orElseThrow().getStatus())
            .isEqualTo(InvitationStatus.EXPIRED);
    }

    @Test
    void administrativePermissionsAllowOnlyCompanyAdmins() throws Exception {
        User nonAdmin = new User();
        nonAdmin.setEmail("manager@example.com");
        nonAdmin.setFirstName("Project");
        nonAdmin.setLastName("Manager");
        nonAdmin.setPasswordHash(passwordEncoder.encode("a-secure-password"));
        nonAdmin.setStatus(UserStatus.ACTIVE);
        nonAdmin.setRoles(EnumSet.of(UserRole.PROJECT_MANAGER));
        nonAdmin = userRepository.save(nonAdmin);

        mockMvc.perform(get("/v2/api/users")
                .with(user(nonAdmin.getEmail()).roles("PROJECT_MANAGER")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error.error_type").value("FORBIDDEN"));

        mockMvc.perform(get("/v2/api/users")
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void projectCreationRequiresTheConfiguredProjectPermission() throws Exception {
        User nonAdmin = new User();
        nonAdmin.setEmail("manager@example.com");
        nonAdmin.setFirstName("Project");
        nonAdmin.setLastName("Manager");
        nonAdmin.setPasswordHash(passwordEncoder.encode("a-secure-password"));
        nonAdmin.setStatus(UserStatus.ACTIVE);
        nonAdmin.setRoles(EnumSet.of(UserRole.PROJECT_MANAGER));
        nonAdmin = userRepository.save(nonAdmin);

        String request = "{\"name\":\"Office\",\"code\":\"OFFICE-1\",\"totalBudget\":1000}";

        mockMvc.perform(post("/v2/api/projects")
                .with(user(nonAdmin.getEmail()).roles("PROJECT_MANAGER"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error.error_type").value("FORBIDDEN"));

        mockMvc.perform(post("/v2/api/projects")
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.projectId").exists());
    }

    @Test
    void phaseAndMemberRoutesRequireTheirProjectPermissions() throws Exception {
        String request = "{\"name\":\"Scoped Project\",\"code\":\"SCOPED-1\",\"totalBudget\":1000}";

        mockMvc.perform(post("/v2/api/projects")
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isCreated())
            .andReturn();

        Long projectId = projectRepository.findByCode("SCOPED-1").orElseThrow().getId();

        User nonAdmin = new User();
        nonAdmin.setEmail("scoped-user@example.com");
        nonAdmin.setFirstName("Scoped");
        nonAdmin.setLastName("User");
        nonAdmin.setPasswordHash(passwordEncoder.encode("a-secure-password"));
        nonAdmin.setStatus(UserStatus.ACTIVE);
        nonAdmin.setRoles(EnumSet.of(UserRole.PROJECT_MANAGER));
        nonAdmin = userRepository.save(nonAdmin);

        mockMvc.perform(get("/v2/api/projects/{projectId}/phases", projectId)
                .with(user(nonAdmin.getEmail()).roles("PROJECT_MANAGER")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error.error_type").value("FORBIDDEN"));

        mockMvc.perform(get("/v2/api/projects/{projectId}/members", projectId)
                .with(user(nonAdmin.getEmail()).roles("PROJECT_MANAGER")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error.error_type").value("FORBIDDEN"));

    }

    private String hash(String rawToken) throws Exception {
        byte[] hash = MessageDigest.getInstance("SHA-256")
            .digest(rawToken.getBytes(StandardCharsets.UTF_8));

        return HexFormat.of().formatHex(hash);
    }
}
