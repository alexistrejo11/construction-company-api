package io.github.alexisTrejo11.construction.company.modules.budget.features.update;

import io.github.alexisTrejo11.construction.company.modules.budget.shared.domain.BudgetStatus;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.dto.BudgetResponse;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.mapper.BudgetMapper;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.persistence.BudgetRepository;
import io.github.alexisTrejo11.construction.company.modules.expense.shared.persistence.ExpenseRepository;
import io.github.alexisTrejo11.construction.company.modules.project.shared.policy.ProjectAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateBudgetHandler {
    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final ProjectAuthorizationPolicy authorizationPolicy;
    private final BudgetMapper mapper;

    @Transactional
    public Result<BudgetResponse> handle(UserContext user, Long budgetId, UpdateBudgetCommand command) {
        var found = budgetRepository.findById(budgetId);
        if (found.isEmpty()) return Result.notFound("Budget was not found");
        var budget = found.get();
        Result<Void> auth = authorizationPolicy.requireProjectPermission(user, budget.getProject().getId(), Permission.BUDGET_UPDATE);
        if (!auth.isSuccess()) return Result.error(auth.getErrorType(), auth.getErrorMessage());
        if (budget.getStatus() != BudgetStatus.DRAFT) return Result.business("Only draft budgets can be updated");
        if (!expenseRepository.findByBudgetIdOrderByCreatedAtDesc(budgetId).isEmpty() && !budget.getCurrency().equals(command.currency())) {
            return Result.conflict("Budget currency cannot change after expenses exist");
        }
        budget.setCurrency(command.currency());
        return Result.success(mapper.toResponse(budgetRepository.save(budget)));
    }
}
