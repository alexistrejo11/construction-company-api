package io.github.alexisTrejo11.construction.company.modules.expense.features.list;

import io.github.alexisTrejo11.construction.company.modules.budget.shared.persistence.BudgetItemRepository;
import io.github.alexisTrejo11.construction.company.modules.expense.shared.domain.ExpenseStatus;
import io.github.alexisTrejo11.construction.company.modules.expense.shared.dto.ExpenseResponse;
import io.github.alexisTrejo11.construction.company.modules.expense.shared.mapper.ExpenseMapper;
import io.github.alexisTrejo11.construction.company.modules.expense.shared.persistence.ExpenseRepository;
import io.github.alexisTrejo11.construction.company.modules.project.shared.policy.ProjectAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.PageResponse;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListExpensesHandler {
    private final ExpenseRepository expenseRepository;
    private final BudgetItemRepository itemRepository;
    private final ProjectAuthorizationPolicy authorizationPolicy;
    private final ExpenseMapper mapper;

    @Transactional(readOnly = true)
    public Result<PageResponse<ExpenseResponse>> handle(UserContext user, Long budgetId, String search, ExpenseStatus status, String currency, int page, int size) {
        var budgetItems = itemRepository.findByBudgetIdOrderByCreatedAtAsc(budgetId);
        var expenses = expenseRepository.findByBudgetIdOrderByCreatedAtDesc(budgetId);
        if (budgetItems.isEmpty() && expenses.isEmpty()) return Result.notFound("Budget was not found");
        Long projectId = expenses.isEmpty() ? budgetItems.getFirst().getBudget().getProject().getId() : expenses.getFirst().getBudget().getProject().getId();
        Result<Void> auth = authorizationPolicy.requireProjectPermission(user, projectId, Permission.EXPENSE_READ);
        if (!auth.isSuccess()) return Result.error(auth.getErrorType(), auth.getErrorMessage());
        if (page < 1 || size < 1 || size > 100) return Result.validation("Invalid pagination parameters");
        var filtered = expenses.stream().filter(expense -> status == null || expense.getStatus() == status)
            .filter(expense -> currency == null || currency.equalsIgnoreCase(expense.getCurrency()))
            .filter(expense -> search == null || (expense.getDescription() != null && expense.getDescription().toLowerCase().contains(search.toLowerCase())))
            .map(mapper::toResponse).toList();
        int from = Math.min((page - 1) * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        return Result.success(new PageResponse<>(filtered.subList(from, to), page, size, filtered.size(), (filtered.size() + size - 1) / size));
    }
}
