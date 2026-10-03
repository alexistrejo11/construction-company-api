package io.github.alexisTrejo11.construction.company.modules.project.features.getglobalsummary;

import io.github.alexisTrejo11.construction.company.modules.project.shared.dto.ProjectsGlobalSummaryResponse;
import io.github.alexisTrejo11.construction.company.modules.project.shared.persistence.ProjectRepository;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.GlobalAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetGlobalProjectSummaryHandler {
    private final ProjectRepository projectRepository;
    private final GlobalAuthorizationPolicy authorizationPolicy;

    @Transactional(readOnly = true)
    public Result<ProjectsGlobalSummaryResponse> execute(UserContext user) {
        Result<Void> authorization = authorizationPolicy.requirePermission(
            user,
            Permission.PROJECT_GLOBAL_SUMMARY
        );
        if (!authorization.isSuccess()) {
            return Result.error(authorization.getErrorType(), authorization.getErrorMessage());
        }

        return Result.success(projectRepository.getGlobalSummary());
    }
}
