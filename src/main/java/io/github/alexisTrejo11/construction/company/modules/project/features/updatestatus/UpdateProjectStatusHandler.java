package io.github.alexisTrejo11.construction.company.modules.project.features.updatestatus;

import io.github.alexisTrejo11.construction.company.modules.project.shared.dto.ProjectResponse;
import io.github.alexisTrejo11.construction.company.modules.project.shared.mapper.ProjectResponseMapper;
import io.github.alexisTrejo11.construction.company.modules.project.shared.persistence.ProjectRepository;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.modules.project.shared.policy.ProjectAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateProjectStatusHandler {
  private final ProjectRepository repository;
  private final ProjectResponseMapper mapper;
  private final ProjectAuthorizationPolicy authorizationPolicy;

  @Transactional
  public Result<ProjectResponse> execute(
      UserContext user,
      Long projectId,
      UpdateProjectStatusCommand command
  ) {
    Result<Void> authorization = authorizationPolicy.requireProjectPermission(
        user,
        projectId,
        Permission.PROJECT_CHANGE_STATUS
    );
    if (!authorization.isSuccess()) {
      return Result.forbidden(authorization.getErrorMessage());
    }

    return repository.findById(projectId)
        .map(project -> {
          project.updateStatus(command.status());
          return Result.success(mapper.toResponse(repository.save(project)));
        })
        .orElseGet(() -> Result.notFound("Project not found"));
  }
}
