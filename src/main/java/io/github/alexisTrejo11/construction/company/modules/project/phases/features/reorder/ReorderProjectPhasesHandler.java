package io.github.alexisTrejo11.construction.company.modules.project.phases.features.reorder;

import org.springframework.stereotype.Service;
import io.github.alexisTrejo11.construction.company.modules.project.shared.policy.ProjectAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReorderProjectPhasesHandler {
  private final ProjectAuthorizationPolicy authorizationPolicy;

  public Result<Void> execute(
      UserContext user,
      Long projectId,
      ReorderProjectPhasesCommand command) {
    Result<Void> authorization = authorizationPolicy.requireProjectPermission(
        user,
        projectId,
        Permission.PHASE_REORDER
    );
    if (!authorization.isSuccess()) {
      return Result.forbidden(authorization.getErrorMessage());
    }

    throw new UnsupportedOperationException("Not supported yet.");
  }
}
