package io.github.alexisTrejo11.construction.company.modules.budget.features.create;

import io.github.alexisTrejo11.construction.company.modules.budget.shared.domain.Budget;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.dto.BudgetResponse;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.mapper.BudgetMapper;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.persistence.BudgetRepository;
import io.github.alexisTrejo11.construction.company.modules.project.shared.persistence.ProjectRepository;
import io.github.alexisTrejo11.construction.company.modules.project.shared.policy.ProjectAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateBudgetHandler {
    private final BudgetRepository budgetRepository;
    private final ProjectRepository projectRepository;
    private final ProjectAuthorizationPolicy authorizationPolicy;
    private final BudgetMapper mapper;

    @Transactional
    public Result<BudgetResponse> handle(UserContext user, Long projectId, CreateBudgetCommand command) {
        Result<Void> auth = authorizationPolicy.requireProjectPermission(user, projectId, Permission.BUDGET_CREATE);
        if (!auth.isSuccess()) return Result.error(auth.getErrorType(), auth.getErrorMessage());
        if (budgetRepository.findByProjectId(projectId).isPresent()) return Result.conflict("Project already has a budget");
        var project = projectRepository.findById(projectId);
        if (project.isEmpty()) return Result.notFound("Project not found");
        var budget = new Budget();
        budget.setProject(project.get());
        budget.setCurrency(command.currency());
        return Result.success(mapper.toResponse(budgetRepository.save(budget)));
    }
}
