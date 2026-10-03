package io.github.alexisTrejo11.construction.company.modules.budget;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.alexisTrejo11.construction.company.modules.budget.shared.persistence.BudgetItemRepository;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.persistence.BudgetRepository;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.persistence.AttachmentRepository;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.persistence.EvidenceRepository;
import io.github.alexisTrejo11.construction.company.modules.expense.shared.persistence.ExpenseRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PhaseEightBudgetExpenseIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private InvitationRepository invitationRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private ProjectMemberRepository memberRepository;
    @Autowired private ProjectPhaseRepository phaseRepository;
    @Autowired private EvidenceRepository evidenceRepository;
    @Autowired private AttachmentRepository attachmentRepository;
    @Autowired private BudgetRepository budgetRepository;
    @Autowired private BudgetItemRepository itemRepository;
    @Autowired private ExpenseRepository expenseRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private User admin;

    @BeforeEach
    void setUp() {
        attachmentRepository.deleteAll();
        evidenceRepository.deleteAll();
        expenseRepository.deleteAll();
        itemRepository.deleteAll();
        budgetRepository.deleteAll();
        invitationRepository.deleteAll();
        phaseRepository.deleteAll();
        memberRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
        admin = userRepository.save(newUser("budget-admin@example.com"));
    }

    @Test
    void budgetItemsAndExpensesFollowLifecycleAndCurrencyRules() throws Exception {
        Long projectId = createProject();
        mockMvc.perform(post("/v2/api/projects/{projectId}/budget", projectId)
                .with(adminRequest()).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"currency\":\"USD\"}"))
            .andExpect(status().isCreated());
        Long budgetId = budgetRepository.findByProjectId(projectId).orElseThrow().getId();

        mockMvc.perform(post("/v2/api/budgets/{budgetId}/items", budgetId)
                .with(adminRequest()).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"description\":\"Concrete\",\"unit\":\"m3\",\"plannedQuantity\":10,\"unitPrice\":25}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.plannedTotal").value(250));
        Long itemId = itemRepository.findByBudgetIdOrderByCreatedAtAsc(budgetId).getFirst().getId();
        mockMvc.perform(post("/v2/api/budgets/{budgetId}/items", budgetId)
                .with(adminRequest()).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"description\":\"Steel\",\"category\":\"materials\",\"plannedQuantity\":2,\"unitPrice\":50}"))
            .andExpect(status().isCreated());
        mockMvc.perform(get("/v2/api/budgets/{budgetId}/items?category=materials&page=1&size=1", budgetId)
                .with(adminRequest()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items.length()").value(1))
            .andExpect(jsonPath("$.data.totalItems").value(1))
            .andExpect(jsonPath("$.data.page").value(1));

        mockMvc.perform(post("/v2/api/budgets/{budgetId}/approve", budgetId)
                .with(adminRequest()).with(csrf())).andExpect(status().isOk());

        mockMvc.perform(post("/v2/api/budgets/{budgetId}/expenses", budgetId)
                .with(adminRequest()).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"budgetItemId\":" + itemId + ",\"amount\":100,\"currency\":\"EUR\"}"))
            .andExpect(status().isBadRequest());
        mockMvc.perform(post("/v2/api/budgets/{budgetId}/expenses", budgetId)
                .with(adminRequest()).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"budgetItemId\":" + itemId + ",\"amount\":100,\"currency\":\"USD\"}"))
            .andExpect(status().isCreated());
        Long expenseId = expenseRepository.findByBudgetItemId(itemId).getFirst().getId();
        mockMvc.perform(get("/v2/api/budgets/{budgetId}/expenses?status=DRAFT&page=1&size=1", budgetId)
                .with(adminRequest()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items.length()").value(1))
            .andExpect(jsonPath("$.data.totalItems").value(1));

        mockMvc.perform(post("/v2/api/expenses/{expenseId}/submit", expenseId).with(adminRequest()).with(csrf()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("PENDING_APPROVAL"));
        mockMvc.perform(post("/v2/api/expenses/{expenseId}/approve", expenseId).with(adminRequest()).with(csrf()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("APPROVED"));
        mockMvc.perform(get("/v2/api/budgets/{budgetId}/summary", budgetId).with(adminRequest()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.plannedAmount").value(350))
            .andExpect(jsonPath("$.data.executedAmount").value(100))
            .andExpect(jsonPath("$.data.overBudget").value(false));

        assertThat(expenseRepository.findById(expenseId).orElseThrow().getStatus().name()).isEqualTo("APPROVED");
    }

    @Test
    void budgetRoutesExposeValidationCsrfAndReadOnlyLifecycleRules() throws Exception {
        Long projectId = createProject();
        mockMvc.perform(post("/v2/api/projects/{projectId}/budget", projectId)
                .with(adminRequest()).contentType(MediaType.APPLICATION_JSON).content("{\"currency\":\"USD\"}"))
            .andExpect(status().isForbidden()).andExpect(jsonPath("$.error").exists());
        mockMvc.perform(post("/v2/api/projects/{projectId}/budget", projectId)
                .with(adminRequest()).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").exists());
        mockMvc.perform(post("/v2/api/projects/{projectId}/budget", projectId)
                .with(adminRequest()).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"currency\":\"USD\"}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.data").exists());
        Long budgetId = budgetRepository.findByProjectId(projectId).orElseThrow().getId();
        mockMvc.perform(post("/v2/api/budgets/{budgetId}/approve", budgetId)
                .with(adminRequest()).with(csrf())).andExpect(status().isOk());
        mockMvc.perform(post("/v2/api/budgets/{budgetId}/close", budgetId)
                .with(adminRequest()).with(csrf())).andExpect(status().isOk());
        mockMvc.perform(patch("/v2/api/budgets/{budgetId}", budgetId)
                .with(adminRequest()).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"currency\":\"EUR\"}"))
            .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.error").exists());
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor adminRequest() {
        return user(admin.getEmail()).roles("COMPANY_ADMIN");
    }

    private Long createProject() throws Exception {
        mockMvc.perform(post("/v2/api/projects").with(adminRequest()).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Budget Project\",\"code\":\"BUDGET-1\",\"totalBudget\":1000}"))
            .andExpect(status().isCreated());
        return projectRepository.findByCode("BUDGET-1").orElseThrow().getId();
    }

    private User newUser(String email) {
        var user = new User();
        user.setEmail(email);
        user.setFirstName("Budget");
        user.setLastName("Admin");
        user.setPasswordHash(passwordEncoder.encode("a-secure-password"));
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(EnumSet.of(UserRole.COMPANY_ADMIN));
        return user;
    }
}
