package io.github.alexisTrejo11.construction.company.modules.project.features.getbycode;

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
public class GetProjectByCodeHandler {
  private final ProjectRepository repository;
  private final ProjectResponseMapper mapper;
  private final ProjectAuthorizationPolicy authorizationPolicy;

  @Transactional(readOnly = true)
  public Result<ProjectResponse> execute(UserContext user, GetProjectByCodeQuery query) {
    var project = repository.findByCode(query.code());
    if (project.isEmpty()) {
      return Result.notFound("Project not found");
    }

    Result<Void> authorization = authorizationPolicy.requireProjectPermission(
        user,
        project.get().getId(),
        Permission.PROJECT_READ
    );
    if (!authorization.isSuccess()) {
      return Result.forbidden(authorization.getErrorMessage());
    }

    return Result.success(mapper.toResponse(project.get()));
  }
}
