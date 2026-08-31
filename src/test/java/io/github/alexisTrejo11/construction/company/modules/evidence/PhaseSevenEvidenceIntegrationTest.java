package io.github.alexisTrejo11.construction.company.modules.evidence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.alexisTrejo11.construction.company.modules.evidence.shared.persistence.AttachmentRepository;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.persistence.EvidenceRepository;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.storage.FileStorage;
import io.github.alexisTrejo11.construction.company.modules.project.members.shared.persistence.ProjectMemberRepository;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PhaseSevenEvidenceIntegrationTest {
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
    private ProjectPhaseRepository phaseRepository;

    @Autowired
    private EvidenceRepository evidenceRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private FileStorage fileStorage;

    private User admin;

    @BeforeEach
    void setUp() {
        attachmentRepository.deleteAll();
        evidenceRepository.deleteAll();
        invitationRepository.deleteAll();
        phaseRepository.deleteAll();
        memberRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        admin = userRepository.save(newUser("evidence-admin@example.com", UserRole.COMPANY_ADMIN));
    }

    @Test
    void evidenceCrudAndAttachmentLifecyclePreservePhaseStatus() throws Exception {
        Long projectId = createProject();
        Long phaseId = createPhase(projectId);

        mockMvc.perform(post("/v2/api/projects/{projectId}/phases/{phaseId}/evidence", projectId, phaseId)
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(evidenceJson("Foundation inspection")))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.phaseId").value(phaseId))
            .andExpect(jsonPath("$.data.title").value("Foundation inspection"));

        Long evidenceId = evidenceRepository.findByPhaseIdOrderByRecordedAtDesc(phaseId).getFirst().getId();

        mockMvc.perform(get("/v2/api/projects/{projectId}/phases/{phaseId}/evidence", projectId, phaseId)
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1));

        mockMvc.perform(get("/v2/api/evidence/{evidenceId}", evidenceId)
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(evidenceId));

        mockMvc.perform(patch("/v2/api/evidence/{evidenceId}", evidenceId)
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(evidenceJson("Updated inspection")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value("Updated inspection"));

        when(fileStorage.store(any())).thenReturn("phase-seven-attachment");
        var file = new MockMultipartFile("file", "inspection.pdf", "application/pdf", "evidence".getBytes());
        mockMvc.perform(multipart("/v2/api/evidence/{evidenceId}/attachments", evidenceId)
                .file(file)
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.originalFileName").value("inspection.pdf"))
            .andExpect(jsonPath("$.data.storageKey").doesNotExist());

        Long attachmentId = attachmentRepository.findByEvidenceIdOrderByCreatedAtAsc(evidenceId).getFirst().getId();
        mockMvc.perform(get("/v2/api/evidence/{evidenceId}/attachments", evidenceId)
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1));

        mockMvc.perform(delete("/v2/api/attachments/{attachmentId}", attachmentId)
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf()))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));
        verify(fileStorage).delete("phase-seven-attachment");

        mockMvc.perform(delete("/v2/api/evidence/{evidenceId}", evidenceId)
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf()))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));

        assertThat(evidenceRepository.findById(evidenceId)).isEmpty();
        assertThat(phaseRepository.findById(phaseId).orElseThrow().getStatus().name()).isEqualTo("PLANNED");
    }

    @Test
    void evidenceValidationUploadValidationAndAuthorizationFailuresAreReported() throws Exception {
        Long projectId = createProject();
        Long phaseId = createPhase(projectId);

        mockMvc.perform(post("/v2/api/projects/{projectId}/phases/{phaseId}/evidence", projectId, phaseId)
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"\",\"recordedAt\":\"2026-08-27T12:00:00Z\"}"))
            .andExpect(status().isBadRequest());

        mockMvc.perform(post("/v2/api/projects/{projectId}/phases/{phaseId}/evidence", projectId, phaseId)
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(evidenceJson("Upload validation")))
            .andExpect(status().isCreated());
        Long evidenceId = evidenceRepository.findByPhaseIdOrderByRecordedAtDesc(phaseId).getFirst().getId();

        var invalidFile = new MockMultipartFile("file", "script.exe", "application/octet-stream", "data".getBytes());
        mockMvc.perform(multipart("/v2/api/evidence/{evidenceId}/attachments", evidenceId)
                .file(invalidFile)
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf()))
            .andExpect(status().isBadRequest());

        User manager = userRepository.save(newUser("evidence-manager@example.com", UserRole.PROJECT_MANAGER));
        mockMvc.perform(get("/v2/api/evidence/{evidenceId}", evidenceId)
                .with(user(manager.getEmail()).roles("PROJECT_MANAGER")))
            .andExpect(status().isForbidden());
    }

    private Long createProject() throws Exception {
        mockMvc.perform(post("/v2/api/projects")
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Evidence Project\",\"code\":\"EVIDENCE-1\",\"totalBudget\":1000}"))
            .andExpect(status().isCreated());
        return projectRepository.findByCode("EVIDENCE-1").orElseThrow().getId();
    }

    private Long createPhase(Long projectId) throws Exception {
        mockMvc.perform(post("/v2/api/projects/{projectId}/phases", projectId)
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Evidence Phase\",\"sequenceOrder\":1,\"allocatedBudget\":1000}"))
            .andExpect(status().isCreated());
        return phaseRepository.findByProjectIdOrderBySequenceOrder(projectId).getFirst().getId();
    }

    private User newUser(String email, UserRole role) {
        var user = new User();
        user.setEmail(email);
        user.setFirstName("Evidence");
        user.setLastName("User");
        user.setPasswordHash(passwordEncoder.encode("a-secure-password"));
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(EnumSet.of(role));
        return user;
    }

    private String evidenceJson(String title) {
        return "{\"title\":\"" + title + "\",\"description\":\"Inspection notes\",\"recordedAt\":\"2026-08-27T12:00:00Z\"}";
    }
}
