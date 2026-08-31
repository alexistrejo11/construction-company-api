package io.github.alexisTrejo11.construction.company.modules.expense.features.create;

import io.github.alexisTrejo11.construction.company.modules.budget.shared.domain.BudgetStatus;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.persistence.BudgetItemRepository;
import io.github.alexisTrejo11.construction.company.modules.expense.shared.domain.Expense;
import io.github.alexisTrejo11.construction.company.modules.expense.shared.dto.ExpenseResponse;
import io.github.alexisTrejo11.construction.company.modules.expense.shared.mapper.ExpenseMapper;
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
public class CreateExpenseHandler {
    private final ExpenseRepository expenseRepository;
    private final BudgetItemRepository itemRepository;
    private final ProjectAuthorizationPolicy authorizationPolicy;
    private final ExpenseMapper mapper;

    @Transactional
    public Result<ExpenseResponse> handle(UserContext user, Long budgetId, CreateExpenseCommand command) {
        var item = itemRepository.findById(command.budgetItemId());
        if (item.isEmpty() || !item.get().getBudget().getId().equals(budgetId)) return Result.notFound("Budget item was not found");
        var budget = item.get().getBudget();
        Result<Void> auth = authorizationPolicy.requireProjectPermission(user, budget.getProject().getId(), Permission.EXPENSE_CREATE);
        if (!auth.isSuccess()) return Result.error(auth.getErrorType(), auth.getErrorMessage());
        if (budget.getStatus() == BudgetStatus.CLOSED) return Result.business("Closed budgets cannot record expenses");
        if (!budget.getCurrency().equals(command.currency())) return Result.validation("Expense currency must match the budget currency");
        var expense = new Expense(); expense.setProject(budget.getProject()); expense.setBudget(budget); expense.setBudgetItem(item.get());
        expense.setAmount(command.amount()); expense.setCurrency(command.currency()); expense.setDescription(command.description());
        return Result.success(mapper.toResponse(expenseRepository.save(expense)));
    }
}
