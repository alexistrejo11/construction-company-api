package io.github.alexisTrejo11.construction.company.modules.budget.features.revise;

import io.github.alexisTrejo11.construction.company.modules.budget.shared.dto.BudgetResponse;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.mapper.BudgetMapper;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.persistence.BudgetRepository;
import io.github.alexisTrejo11.construction.company.modules.project.shared.policy.ProjectAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviseBudgetHandler {
    private final BudgetRepository budgetRepository;
    private final ProjectAuthorizationPolicy authorizationPolicy;
    private final BudgetMapper mapper;

    @Transactional
    public Result<BudgetResponse> handle(UserContext user, Long budgetId) {
        var found = budgetRepository.findById(budgetId);
        if (found.isEmpty()) return Result.notFound("Budget was not found");
        var budget = found.get();
        Result<Void> auth = authorizationPolicy.requireProjectPermission(user, budget.getProject().getId(), Permission.BUDGET_REVISE);
        if (!auth.isSuccess()) return Result.error(auth.getErrorType(), auth.getErrorMessage());
        Result<Void> transition = budget.revise();
        if (!transition.isSuccess()) return Result.error(transition.getErrorType(), transition.getErrorMessage());
        return Result.success(mapper.toResponse(budgetRepository.save(budget)));
    }
}
