package io.github.alexisTrejo11.construction.company.modules.project.features.get;

import io.github.alexisTrejo11.construction.company.modules.project.shared.dto.ProjectResponse;
import io.github.alexisTrejo11.construction.company.modules.project.shared.mapper.ProjectResponseMapper;
import io.github.alexisTrejo11.construction.company.modules.project.shared.persistence.ProjectRepository;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.GlobalAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import io.github.alexisTrejo11.construction.company.shared.dto.PageResponse;
import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetProjectHandler {
  private final ProjectRepository repository;
  private final ProjectResponseMapper mapper;
  private final GlobalAuthorizationPolicy authorizationPolicy;

  @Transactional(readOnly = true)
  public Result<PageResponse<ProjectResponse>> execute(UserContext user, GetProjectQuery query) {
    Result<Void> authorization = authorizationPolicy.requirePermission(user, Permission.PROJECT_READ);
    if (!authorization.isSuccess()) {
      return Result.forbidden(authorization.getErrorMessage());
    }

    if (query.pageRequest().page() < 1 || query.pageRequest().size() < 1 || query.pageRequest().size() > 100) {
      return Result.validation("Invalid pagination parameters");
    }

    Page<ProjectResponse> projects;
    if (user.roles().contains(UserRole.COMPANY_ADMIN)) {
      projects = repository.findWithFilters(
          query.search(),
          query.status(),
          query.city(),
          query.pageRequest().toPageable()
      ).map(mapper::toResponse);
    } else {
      projects = repository.findVisibleByUser(
          user.userId(),
          query.search(),
          query.status(),
          query.city(),
          query.pageRequest().toPageable()
      ).map(mapper::toResponse);
    }

    return Result.success(new PageResponse<>(
        projects.getContent(),
        projects.getNumber() + 1,
        projects.getSize(),
        projects.getTotalElements(),
        projects.getTotalPages()
    ));
  }
}
