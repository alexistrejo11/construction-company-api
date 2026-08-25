package io.github.alexisTrejo11.construction.company.modules.project.features.restore;

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
public class RestoreProjectHandler {
    private final ProjectRepository projectRepository;
    private final ProjectAuthorizationPolicy authorizationPolicy;

    @Transactional
    public Result<RestoreProjectResponse> execute(UserContext user, Long projectId) {
        var project = projectRepository.findById(projectId);
        if (project.isEmpty()) {
            return Result.notFound("Project not found");
        }

        Result<Void> authorization = authorizationPolicy.requireProjectPermission(
            user,
            projectId,
            Permission.PROJECT_RESTORE
        );
        if (!authorization.isSuccess()) {
            return Result.error(authorization.getErrorType(), authorization.getErrorMessage());
        }

        Result<Void> restored = project.get().restore();
        if (!restored.isSuccess()) {
            return Result.error(restored.getErrorType(), restored.getErrorMessage());
        }

        projectRepository.save(project.get());
        return Result.success(new RestoreProjectResponse(projectId, project.get().getStatus()));
    }
}
