package io.github.alexisTrejo11.construction.company.modules.budget.features.summary;

import io.github.alexisTrejo11.construction.company.modules.budget.shared.dto.BudgetSummaryResponse;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.persistence.BudgetItemRepository;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.persistence.BudgetRepository;
import io.github.alexisTrejo11.construction.company.modules.expense.shared.domain.ExpenseStatus;
import io.github.alexisTrejo11.construction.company.modules.expense.shared.persistence.ExpenseRepository;
import io.github.alexisTrejo11.construction.company.modules.project.shared.policy.ProjectAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetBudgetSummaryHandler {
    private final BudgetRepository budgetRepository;
    private final BudgetItemRepository itemRepository;
    private final ExpenseRepository expenseRepository;
    private final ProjectAuthorizationPolicy authorizationPolicy;

    @Transactional(readOnly = true)
    public Result<BudgetSummaryResponse> handle(UserContext user, Long budgetId) {
        var found = budgetRepository.findById(budgetId);
        if (found.isEmpty()) return Result.notFound("Budget was not found");
        var budget = found.get();
        Result<Void> auth = authorizationPolicy.requireProjectPermission(user, budget.getProject().getId(), Permission.BUDGET_READ);
        if (!auth.isSuccess()) return Result.error(auth.getErrorType(), auth.getErrorMessage());
        BigDecimal planned = itemRepository.findByBudgetIdOrderByCreatedAtAsc(budgetId).stream().map(item -> item.plannedTotal()).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal executed = expenseRepository.findByBudgetIdOrderByCreatedAtDesc(budgetId).stream().filter(expense -> expense.getStatus() == ExpenseStatus.APPROVED).map(expense -> expense.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
        return Result.success(new BudgetSummaryResponse(budgetId, budget.getCurrency(), planned, executed, planned.subtract(executed), executed.subtract(planned), executed.compareTo(planned) > 0));
    }
}
