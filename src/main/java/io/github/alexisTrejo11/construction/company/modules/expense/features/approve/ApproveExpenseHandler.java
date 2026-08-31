package io.github.alexisTrejo11.construction.company.modules.expense.features.approve;

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
public class ApproveExpenseHandler {
    private final ExpenseRepository repository;
    private final ProjectAuthorizationPolicy policy;
    private final ExpenseMapper mapper;

    @Transactional
    public Result<ExpenseResponse> handle(UserContext user, Long id) {
        var found = repository.findById(id);
        if (found.isEmpty()) return Result.notFound("Expense was not found");
        var expense = found.get();
        Result<Void> auth = policy.requireProjectPermission(user, expense.getProject().getId(), Permission.EXPENSE_APPROVE);
        if (!auth.isSuccess()) return Result.error(auth.getErrorType(), auth.getErrorMessage());
        Result<Void> transition = expense.approve();
        if (!transition.isSuccess()) return Result.error(transition.getErrorType(), transition.getErrorMessage());
        return Result.success(mapper.toResponse(repository.save(expense)));
    }
}
