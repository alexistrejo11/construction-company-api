package io.github.alexisTrejo11.construction.company.modules.project.phases.features.create;

import io.github.alexisTrejo11.construction.company.modules.project.shared.dto.ProjectPhaseResponse;
import io.github.alexisTrejo11.construction.company.modules.project.shared.policy.ProjectAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateProjectPhaseHandler {
  private final ProjectAuthorizationPolicy authorizationPolicy;

  public Result<ProjectPhaseResponse> execute(
      UserContext user,
      Long projectId,
      CreateProjectPhaseCommand command) {
    Result<Void> authorization = authorizationPolicy.requireProjectPermission(
        user,
        projectId,
        Permission.PHASE_CREATE
    );
    if (!authorization.isSuccess()) {
      return Result.forbidden(authorization.getErrorMessage());
    }

    throw new UnsupportedOperationException("Not supported yet.");
  }
}
