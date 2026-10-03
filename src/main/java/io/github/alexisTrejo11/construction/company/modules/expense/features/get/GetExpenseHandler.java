package io.github.alexisTrejo11.construction.company.modules.expense.features.get;

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
public class GetExpenseHandler {
    private final ExpenseRepository expenseRepository;
    private final ProjectAuthorizationPolicy authorizationPolicy;
    private final ExpenseMapper mapper;

    @Transactional(readOnly = true)
    public Result<ExpenseResponse> handle(UserContext user, Long expenseId) {
        var found = expenseRepository.findById(expenseId);
        if (found.isEmpty()) return Result.notFound("Expense was not found");
        Result<Void> auth = authorizationPolicy.requireProjectPermission(user, found.get().getProject().getId(), Permission.EXPENSE_READ);
        if (!auth.isSuccess()) return Result.error(auth.getErrorType(), auth.getErrorMessage());
        return Result.success(mapper.toResponse(found.get()));
    }
}
