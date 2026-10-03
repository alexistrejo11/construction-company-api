package io.github.alexisTrejo11.construction.company.modules.budget.features.getbyproject;

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
public class GetBudgetByProjectHandler {
    private final BudgetRepository budgetRepository;
    private final ProjectAuthorizationPolicy authorizationPolicy;
    private final BudgetMapper mapper;

    @Transactional(readOnly = true)
    public Result<BudgetResponse> handle(UserContext user, Long projectId) {
        Result<Void> auth = authorizationPolicy.requireProjectPermission(user, projectId, Permission.BUDGET_READ);
        if (!auth.isSuccess()) return Result.error(auth.getErrorType(), auth.getErrorMessage());
        return budgetRepository.findByProjectId(projectId).map(budget -> Result.success(mapper.toResponse(budget)))
            .orElseGet(() -> Result.notFound("Budget was not found"));
    }
}
