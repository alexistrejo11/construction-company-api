package io.github.alexisTrejo11.construction.company.modules.project.features.getmyprojects;

import io.github.alexisTrejo11.construction.company.modules.project.shared.dto.ProjectResponse;
import io.github.alexisTrejo11.construction.company.modules.project.shared.mapper.ProjectResponseMapper;
import io.github.alexisTrejo11.construction.company.modules.project.shared.persistence.ProjectRepository;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.GlobalAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetMyProjectsHandler {
  private final ProjectRepository repository;
  private final ProjectResponseMapper mapper;
  private final GlobalAuthorizationPolicy authorizationPolicy;

  @Transactional(readOnly = true)
  public Result<Page<ProjectResponse>> execute(UserContext user, GetMyProjectsQuery query) {
    Result<Void> authorization = authorizationPolicy.requirePermission(user, Permission.PROJECT_READ);
    if (!authorization.isSuccess()) {
      return Result.forbidden(authorization.getErrorMessage());
    }

    return Result.success(repository.findByUserIdWithMembers(query.userId(), query.pageRequest().toPageable())
        .map(mapper::toResponse));
  }
}
