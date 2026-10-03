package io.github.alexisTrejo11.construction.company.modules.expense.features.update;

import io.github.alexisTrejo11.construction.company.modules.expense.shared.domain.ExpenseStatus;
import io.github.alexisTrejo11.construction.company.modules.expense.shared.dto.ExpenseResponse;
import io.github.alexisTrejo11.construction.company.modules.expense.shared.mapper.ExpenseMapper;
import io.github.alexisTrejo11.construction.company.modules.expense.shared.persistence.ExpenseRepository;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.persistence.BudgetItemRepository;
import io.github.alexisTrejo11.construction.company.modules.project.shared.policy.ProjectAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateExpenseHandler {
    private final ExpenseRepository expenseRepository;
    private final BudgetItemRepository itemRepository;
    private final ProjectAuthorizationPolicy authorizationPolicy;
    private final ExpenseMapper mapper;

    @Transactional
    public Result<ExpenseResponse> handle(UserContext user, Long expenseId, UpdateExpenseCommand command) {
        var found = expenseRepository.findById(expenseId);
        if (found.isEmpty()) return Result.notFound("Expense was not found");
        var expense = found.get();
        Result<Void> auth = authorizationPolicy.requireProjectPermission(user, expense.getProject().getId(), Permission.EXPENSE_UPDATE);
        if (!auth.isSuccess()) return Result.error(auth.getErrorType(), auth.getErrorMessage());
        if (expense.getStatus() != ExpenseStatus.DRAFT) return Result.business("Only draft expenses can be updated");
        if (!expense.getBudget().getCurrency().equals(command.currency())) return Result.validation("Expense currency must match the budget currency");
        var item = itemRepository.findById(command.budgetItemId());
        if (item.isEmpty() || !item.get().getBudget().getId().equals(expense.getBudget().getId())) return Result.notFound("Budget item was not found");
        expense.setBudgetItem(item.get()); expense.setAmount(command.amount()); expense.setCurrency(command.currency()); expense.setDescription(command.description());
        return Result.success(mapper.toResponse(expenseRepository.save(expense)));
    }
}
