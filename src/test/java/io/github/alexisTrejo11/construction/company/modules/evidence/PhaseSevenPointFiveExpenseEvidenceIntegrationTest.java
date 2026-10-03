package io.github.alexisTrejo11.construction.company.modules.evidence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.alexisTrejo11.construction.company.modules.evidence.shared.persistence.EvidenceRepository;
import io.github.alexisTrejo11.construction.company.modules.expense.shared.domain.Expense;
import io.github.alexisTrejo11.construction.company.modules.expense.shared.domain.ExpenseStatus;
import io.github.alexisTrejo11.construction.company.modules.expense.shared.persistence.ExpenseRepository;
import io.github.alexisTrejo11.construction.company.modules.project.members.shared.persistence.ProjectMemberRepository;
import io.github.alexisTrejo11.construction.company.modules.project.phases.shared.persistence.ProjectPhaseRepository;
import io.github.alexisTrejo11.construction.company.modules.project.shared.persistence.ProjectRepository;
import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.User;
import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.UserRole;
import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.UserStatus;
import io.github.alexisTrejo11.construction.company.modules.user.shared.persistence.InvitationRepository;
import io.github.alexisTrejo11.construction.company.modules.user.shared.persistence.UserRepository;
import java.math.BigDecimal;
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
class PhaseSevenPointFiveExpenseEvidenceIntegrationTest {
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
    private ExpenseRepository expenseRepository;

    @Autowired
    private EvidenceRepository evidenceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User admin;

    @BeforeEach
    void setUp() {
        evidenceRepository.deleteAll();
        expenseRepository.deleteAll();
        invitationRepository.deleteAll();
        phaseRepository.deleteAll();
        memberRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
        admin = userRepository.save(newUser("expense-evidence-admin@example.com", UserRole.COMPANY_ADMIN));
    }

    @Test
    void expenseEvidenceCanBeCreatedAndListedWithoutChangingExpenseState() throws Exception {
        Long projectId = createProject();
        Expense expense = new Expense();
        expense.setProject(projectRepository.findById(projectId).orElseThrow());
        expense.setAmount(BigDecimal.valueOf(250));
        expense.setCurrency("USD");
        expense = expenseRepository.save(expense);

        mockMvc.perform(post("/v2/api/expenses/{expenseId}/evidence", expense.getId())
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(evidenceJson("Expense receipt")))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.expenseId").value(expense.getId()))
            .andExpect(jsonPath("$.data.phaseId").doesNotExist());

        mockMvc.perform(get("/v2/api/expenses/{expenseId}/evidence", expense.getId())
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1));

        assertThat(expenseRepository.findById(expense.getId()).orElseThrow().getStatus()).isEqualTo(ExpenseStatus.DRAFT);
    }

    @Test
    void expenseEvidenceRequiresProjectAccessAndRejectsUnknownExpenses() throws Exception {
        Long projectId = createProject();
        Expense expense = new Expense();
        expense.setProject(projectRepository.findById(projectId).orElseThrow());
        expense = expenseRepository.save(expense);

        User manager = userRepository.save(newUser("expense-evidence-manager@example.com", UserRole.PROJECT_MANAGER));
        mockMvc.perform(get("/v2/api/expenses/{expenseId}/evidence", expense.getId())
                .with(user(manager.getEmail()).roles("PROJECT_MANAGER")))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/v2/api/expenses/{expenseId}/evidence", 999999L)
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN")))
            .andExpect(status().isNotFound());
    }

    private Long createProject() throws Exception {
        mockMvc.perform(post("/v2/api/projects")
                .with(user(admin.getEmail()).roles("COMPANY_ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Expense Evidence Project\",\"code\":\"EXP-EVIDENCE-1\",\"totalBudget\":1000}"))
            .andExpect(status().isCreated());
        return projectRepository.findByCode("EXP-EVIDENCE-1").orElseThrow().getId();
    }

    private User newUser(String email, UserRole role) {
        var user = new User();
        user.setEmail(email);
        user.setFirstName("Expense");
        user.setLastName("Evidence");
        user.setPasswordHash(passwordEncoder.encode("a-secure-password"));
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(EnumSet.of(role));
        return user;
    }

    private String evidenceJson(String title) {
        return "{\"title\":\"" + title + "\",\"description\":\"Receipt\",\"recordedAt\":\"2026-08-27T12:00:00Z\"}";
    }
}
