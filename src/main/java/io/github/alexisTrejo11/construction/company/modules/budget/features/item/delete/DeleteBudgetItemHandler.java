package io.github.alexisTrejo11.construction.company.modules.budget.features.item.delete;

import io.github.alexisTrejo11.construction.company.modules.budget.shared.domain.BudgetStatus;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.persistence.BudgetItemRepository;
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
public class DeleteBudgetItemHandler {
    private final BudgetItemRepository itemRepository;
    private final ExpenseRepository expenseRepository;
    private final ProjectAuthorizationPolicy authorizationPolicy;

    @Transactional
    public Result<Void> handle(UserContext user, Long itemId) {
        var found = itemRepository.findById(itemId);
        if (found.isEmpty()) return Result.notFound("Budget item was not found");
        var item = found.get();
        Result<Void> auth = authorizationPolicy.requireProjectPermission(user, item.getBudget().getProject().getId(), Permission.BUDGET_ITEM_DELETE);
        if (!auth.isSuccess()) return Result.error(auth.getErrorType(), auth.getErrorMessage());
        if (item.getBudget().getStatus() != BudgetStatus.DRAFT) return Result.business("Only draft budgets can change items");
        if (!expenseRepository.findByBudgetItemId(itemId).isEmpty()) return Result.conflict("Budget items with expenses cannot be deleted");
        itemRepository.delete(item);
        return Result.success();
    }
}
