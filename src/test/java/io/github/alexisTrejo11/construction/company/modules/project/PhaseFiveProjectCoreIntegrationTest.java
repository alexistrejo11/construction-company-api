package io.github.alexisTrejo11.construction.company.modules.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.alexisTrejo11.construction.company.modules.project.members.shared.persistence.ProjectMemberRepository;
import io.github.alexisTrejo11.construction.company.modules.project.shared.domain.Project;
import io.github.alexisTrejo11.construction.company.modules.project.shared.domain.ProjectStatus;
import io.github.alexisTrejo11.construction.company.modules.project.shared.persistence.ProjectRepository;
import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.User;
import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.UserRole;
import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.UserStatus;
import io.github.alexisTrejo11.construction.company.modules.user.shared.persistence.InvitationRepository;
import io.github.alexisTrejo11.construction.company.modules.user.shared.persistence.UserRepository;
import java.util.EnumSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PhaseFiveProjectCoreIntegrationTest {
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

    private User admin;

    @BeforeEach
    void setUp() {
        invitationRepository.deleteAll();
        projectMemberRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        admin = new User();
        admin.setEmail("admin@example.com");
        admin.setFirstName("System");
        admin.setLastName("Admin");
        admin.setPasswordHash(passwordEncoder.encode("a-secure-password"));
        admin.setStatus(UserStatus.ACTIVE);
        admin.setRoles(EnumSet.of(UserRole.COMPANY_ADMIN));
        admin = userRepository.save(admin);
    }

    @Test
    void projectCreationCreatesActiveCreatorMembershipAndReturnsEnvelope() throws Exception {
        mockMvc.perform(post("/v2/api/projects")
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(projectJson("CORE-1", "Core Project")))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.projectId").exists())
            .andExpect(jsonPath("$.error").doesNotExist())
            .andExpect(jsonPath("$.traceId").exists())
            .andExpect(header().exists("X-Trace-Id"));

        Project project = projectRepository.findByCode("CORE-1").orElseThrow();
        assertThat(projectMemberRepository.findByProjectIdAndUserId(project.getId(), admin.getId()))
            .isPresent()
            .get()
            .extracting(member -> member.getStatus())
            .isEqualTo(io.github.alexisTrejo11.construction.company.modules.project.members.shared.domain.ProjectMemberStatus.ACTIVE);
    }

    @Test
    void projectCreationRejectsInvalidInputAndDuplicateCode() throws Exception {
        mockMvc.perform(post("/v2/api/projects")
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\",\"code\":\"lower\",\"totalBudget\":-1}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.error_type").value("VALIDATION"));

        createProject("DUP-1", "Duplicate");

        mockMvc.perform(post("/v2/api/projects")
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(projectJson("DUP-1", "Duplicate Again")))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error.error_type").value("CONFLICT"));
    }

    @Test
    void projectListUsesExplicitOneBasedPaginationAndAdminCanReadAll() throws Exception {
        createProject("LIST-1", "List One");
        createProject("LIST-2", "List Two");

        mockMvc.perform(get("/v2/api/projects?page=1&size=1")
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page").value(1))
            .andExpect(jsonPath("$.data.size").value(1))
            .andExpect(jsonPath("$.data.totalItems").value(2))
            .andExpect(jsonPath("$.data.totalPages").value(2));

        mockMvc.perform(get("/v2/api/projects?page=0&size=1")
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.error_type").value("VALIDATION"));
    }

    @Test
    void projectLookupsMyProjectsAndPartialUpdateUseProjectDtos() throws Exception {
        Long projectId = createProject("LOOKUP-1", "Lookup Project");

        mockMvc.perform(get("/v2/api/projects/{projectId}", projectId)
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(projectId))
            .andExpect(jsonPath("$.data.code").value("LOOKUP-1"));

        mockMvc.perform(get("/v2/api/projects/code/{code}", "LOOKUP-1")
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(projectId));

        mockMvc.perform(get("/v2/api/projects/my-projects?page=1&size=20")
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items[0].id").value(projectId));

        mockMvc.perform(patch("/v2/api/projects/{projectId}", projectId)
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Updated Project\",\"description\":\"Updated\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("Updated Project"))
            .andExpect(jsonPath("$.data.code").value("LOOKUP-1"));
    }

    @Test
    void projectStatusLifecycleRejectsInvalidTransitionsAndTerminalUpdates() throws Exception {
        Long projectId = createProject("STATE-1", "State Project");

        changeStatus(projectId, "IN_PROGRESS");
        changeStatus(projectId, "ON_HOLD");
        changeStatus(projectId, "IN_PROGRESS");
        changeStatus(projectId, "COMPLETED");

        mockMvc.perform(patch("/v2/api/projects/{projectId}/status", projectId)
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"PLANNING\"}"))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.error.error_type").value("BUSINESS_RULE"));

        mockMvc.perform(patch("/v2/api/projects/{projectId}", projectId)
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Cannot Edit\"}"))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.error.error_type").value("BUSINESS_RULE"));
    }

    @Test
    void cancelledProjectCanBeRestoredAndSummariesAreProtected() throws Exception {
        Long projectId = createProject("SUMMARY-1", "Summary Project");
        changeStatus(projectId, "CANCELLED");

        mockMvc.perform(post("/v2/api/projects/{projectId}/restore", projectId)
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("PLANNING"));

        mockMvc.perform(get("/v2/api/projects/{projectId}/summary", projectId)
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.projectId").value(projectId))
            .andExpect(jsonPath("$.data.code").value("SUMMARY-1"));

        mockMvc.perform(get("/v2/api/projects/summary")
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalProjects").value(1));
    }

    private Long createProject(String code, String name) throws Exception {
        mockMvc.perform(post("/v2/api/projects")
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(projectJson(code, name)))
            .andExpect(status().isCreated());

        return projectRepository.findByCode(code).orElseThrow().getId();
    }

    private void changeStatus(Long projectId, String status) throws Exception {
        mockMvc.perform(patch("/v2/api/projects/{projectId}/status", projectId)
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"" + status + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value(status));
    }

    private String projectJson(String code, String name) {
        return "{\"name\":\"" + name
            + "\",\"code\":\"" + code
            + "\",\"totalBudget\":1000}";
    }
}
