package io.github.alexisTrejo11.construction.company.modules.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.alexisTrejo11.construction.company.modules.project.members.shared.persistence.ProjectMemberRepository;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.persistence.AttachmentRepository;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.persistence.EvidenceRepository;
import io.github.alexisTrejo11.construction.company.modules.expense.shared.persistence.ExpenseRepository;
import io.github.alexisTrejo11.construction.company.modules.project.phases.shared.persistence.ProjectPhaseRepository;
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
class PhaseSixNestedWorkflowIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMemberRepository memberRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private EvidenceRepository evidenceRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ProjectPhaseRepository phaseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User admin;

    @BeforeEach
    void setUp() {
        attachmentRepository.deleteAll();
        evidenceRepository.deleteAll();
        expenseRepository.deleteAll();
        invitationRepository.deleteAll();
        phaseRepository.deleteAll();
        memberRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        admin = user("admin@example.com", UserRole.COMPANY_ADMIN);
        admin = userRepository.save(admin);
    }

    @Test
    void phaseCrudStatusAndReorderWorkflowPersistsChanges() throws Exception {
        Long projectId = createProject();

        mockMvc.perform(post("/v2/api/projects/{projectId}/phases", projectId)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(phaseJson("Foundation", 1, 1000)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.status").value("PLANNED"));

        mockMvc.perform(post("/v2/api/projects/{projectId}/phases", projectId)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(phaseJson("Structure", 2, 2000)))
            .andExpect(status().isCreated());

        Long firstPhaseId = phaseRepository.findByProjectIdOrderBySequenceOrder(projectId).get(0).getId();
        Long secondPhaseId = phaseRepository.findByProjectIdOrderBySequenceOrder(projectId).get(1).getId();

        mockMvc.perform(get("/v2/api/projects/{projectId}/phases", projectId)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(admin.getEmail()).roles("COMPANY_ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(2));

        mockMvc.perform(get("/v2/api/projects/{projectId}/phases/{phaseId}", projectId, firstPhaseId)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(admin.getEmail()).roles("COMPANY_ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(firstPhaseId));

        mockMvc.perform(patch("/v2/api/projects/{projectId}/phases/{phaseId}", projectId, firstPhaseId)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(phaseJson("Foundation Updated", 1, 1200)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("Foundation Updated"));

        mockMvc.perform(patch("/v2/api/projects/{projectId}/phases/{phaseId}/status", projectId, firstPhaseId)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"IN_PROGRESS\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));

        mockMvc.perform(patch("/v2/api/projects/{projectId}/phases/reorder", projectId)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"phases\":[{\"phaseId\":" + firstPhaseId + ",\"sequenceOrder\":2},{\"phaseId\":" + secondPhaseId + ",\"sequenceOrder\":1}]}"))
            .andExpect(status().isOk());

        assertThat(phaseRepository.findByProjectIdOrderBySequenceOrder(projectId).get(0).getId())
            .isEqualTo(secondPhaseId);

        mockMvc.perform(patch("/v2/api/projects/{projectId}/status", projectId)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"IN_PROGRESS\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(patch("/v2/api/projects/{projectId}/status", projectId)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"COMPLETED\"}"))
            .andExpect(status().isOk());

        assertThat(projectRepository.findById(projectId).orElseThrow().getStatus().name())
            .isEqualTo("COMPLETED");
    }

    @Test
    void memberAddListLookupAndStatusWorkflowPreservesMembershipHistory() throws Exception {
        Long projectId = createProject();
        User member = user("member@example.com", UserRole.PROJECT_MANAGER);
        member = userRepository.save(member);

        mockMvc.perform(post("/v2/api/projects/{projectId}/members", projectId)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":" + member.getId() + "}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.userId").value(member.getId()));

        mockMvc.perform(get("/v2/api/projects/{projectId}/members", projectId)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(admin.getEmail()).roles("COMPANY_ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(2));

        mockMvc.perform(get("/v2/api/projects/{projectId}/members/{userId}", projectId, member.getId())
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(admin.getEmail()).roles("COMPANY_ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.userId").value(member.getId()));

        mockMvc.perform(patch("/v2/api/projects/{projectId}/members/{userId}/status", projectId, member.getId())
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"INACTIVE\"}"))
            .andExpect(status().isOk());

        assertThat(memberRepository.findByProjectIdAndUserId(projectId, member.getId()).orElseThrow().getDeactivatedAt())
            .isNotNull();

        mockMvc.perform(get("/v2/api/projects/{projectId}/phases", projectId)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(member.getEmail()).roles("PROJECT_MANAGER")))
            .andExpect(status().isForbidden());
    }

    @Test
    void nonAdminCannotUseNestedProjectOperationsWithInitialPermissionMatrix() throws Exception {
        Long projectId = createProject();
        User manager = user("manager@example.com", UserRole.PROJECT_MANAGER);
        manager = userRepository.save(manager);

        mockMvc.perform(get("/v2/api/projects/{projectId}/phases", projectId)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(manager.getEmail()).roles("PROJECT_MANAGER")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error.error_type").value("FORBIDDEN"));

        mockMvc.perform(get("/v2/api/projects/{projectId}/members", projectId)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(manager.getEmail()).roles("PROJECT_MANAGER")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error.error_type").value("FORBIDDEN"));
    }

    @Test
    void phaseOrderingRejectsDuplicateUpdatesAndUnknownReorderIds() throws Exception {
        Long projectId = createProject();

        mockMvc.perform(post("/v2/api/projects/{projectId}/phases", projectId)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(phaseJson("First", 1, 100)))
            .andExpect(status().isCreated());
        mockMvc.perform(post("/v2/api/projects/{projectId}/phases", projectId)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(phaseJson("Second", 2, 100)))
            .andExpect(status().isCreated());

        var phases = phaseRepository.findByProjectIdOrderBySequenceOrder(projectId);
        Long firstPhaseId = phases.get(0).getId();
        Long secondPhaseId = phases.get(1).getId();

        mockMvc.perform(patch("/v2/api/projects/{projectId}/phases/{phaseId}", projectId, secondPhaseId)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(phaseJson("Second", 1, 100)))
            .andExpect(status().isConflict());

        mockMvc.perform(patch("/v2/api/projects/{projectId}/phases/reorder", projectId)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"phases\":[{\"phaseId\":999999,\"sequenceOrder\":1},{\"phaseId\":" + firstPhaseId + ",\"sequenceOrder\":2}]}"))
            .andExpect(status().isBadRequest());
    }

    private Long createProject() throws Exception {
        mockMvc.perform(post("/v2/api/projects")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Nested Project\",\"code\":\"NESTED-1\",\"totalBudget\":1000}"))
            .andExpect(status().isCreated());

        return projectRepository.findByCode("NESTED-1").orElseThrow().getId();
    }

    private User user(String email, UserRole role) {
        var user = new User();
        user.setEmail(email);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPasswordHash(passwordEncoder.encode("a-secure-password"));
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(EnumSet.of(role));
        return user;
    }

    private String phaseJson(String name, int order, int budget) {
        return "{\"name\":\"" + name
            + "\",\"sequenceOrder\":" + order
            + ",\"allocatedBudget\":" + budget + "}";
    }
}
