package io.github.alexisTrejo11.construction.company.modules.evidence.shared.access;

import io.github.alexisTrejo11.construction.company.modules.evidence.shared.domain.Evidence;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.persistence.EvidenceRepository;
import io.github.alexisTrejo11.construction.company.modules.project.phases.shared.domain.ProjectPhase;
import io.github.alexisTrejo11.construction.company.modules.project.phases.shared.persistence.ProjectPhaseRepository;
import io.github.alexisTrejo11.construction.company.modules.expense.shared.domain.Expense;
import io.github.alexisTrejo11.construction.company.modules.expense.shared.persistence.ExpenseRepository;
import io.github.alexisTrejo11.construction.company.modules.project.shared.policy.ProjectAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EvidenceAccess {
    private final EvidenceRepository evidenceRepository;
    private final ProjectPhaseRepository phaseRepository;
    private final ProjectAuthorizationPolicy authorizationPolicy;
    private final ExpenseRepository expenseRepository;

    public Result<ProjectPhase> requirePhase(
        UserContext user,
        Long projectId,
        Long phaseId,
        Permission permission
    ) {
        Result<Void> authorization = authorizationPolicy.requireProjectPermission(user, projectId, permission);
        if (!authorization.isSuccess()) {
            return Result.error(authorization.getErrorType(), authorization.getErrorMessage());
        }

        return phaseRepository.findByIdAndProjectId(phaseId, projectId)
            .map(Result::success)
            .orElseGet(() -> Result.notFound("Project phase was not found"));
    }

    public Result<Evidence> requireEvidence(
        UserContext user,
        Long evidenceId,
        Permission permission
    ) {
        var evidence = evidenceRepository.findById(evidenceId);
        if (evidence.isEmpty()) {
            return Result.notFound("Evidence was not found");
        }

        Long projectId = evidence.get().getPhase() != null
            ? evidence.get().getPhase().getProject().getId()
            : evidence.get().getExpense().getProject().getId();
        Result<Void> authorization = authorizationPolicy.requireProjectPermission(user, projectId, permission);
        if (!authorization.isSuccess()) {
            return Result.error(authorization.getErrorType(), authorization.getErrorMessage());
        }

        return Result.success(evidence.get());
    }

    public Result<Expense> requireExpense(
        UserContext user,
        Long expenseId,
        Permission permission
    ) {
        return expenseRepository.findById(expenseId)
            .map(expense -> authorizeExpense(user, expense, permission))
            .orElseGet(() -> Result.notFound("Expense was not found"));
    }

    private Result<Expense> authorizeExpense(UserContext user, Expense expense, Permission permission) {
        Result<Void> authorization = authorizationPolicy.requireProjectPermission(
            user,
            expense.getProject().getId(),
            permission
        );
        if (!authorization.isSuccess()) {
            return Result.error(authorization.getErrorType(), authorization.getErrorMessage());
        }
        return Result.success(expense);
    }
}
