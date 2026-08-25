package io.github.alexisTrejo11.construction.company.modules.project.features.getsummary;

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
public class GetProjectSummaryHandler {
    private final ProjectRepository projectRepository;
    private final ProjectAuthorizationPolicy authorizationPolicy;

    @Transactional(readOnly = true)
    public Result<ProjectSummaryResponse> execute(UserContext user, Long projectId) {
        var project = projectRepository.findById(projectId);
        if (project.isEmpty()) {
            return Result.notFound("Project not found");
        }

        Result<Void> authorization = authorizationPolicy.requireProjectPermission(
            user,
            projectId,
            Permission.PROJECT_READ
        );
        if (!authorization.isSuccess()) {
            return Result.error(authorization.getErrorType(), authorization.getErrorMessage());
        }

        var value = project.get();
        return Result.success(new ProjectSummaryResponse(
            value.getId(),
            value.getName(),
            value.getCode(),
            value.getStatus(),
            value.getTotalBudget(),
            value.getStartDate(),
            value.getEstimatedEndDate(),
            value.getActualEndDate()
        ));
    }
}
