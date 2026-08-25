package io.github.alexisTrejo11.construction.company.modules.project.phases.features.get;

import io.github.alexisTrejo11.construction.company.modules.project.shared.dto.ProjectPhaseResponse;
import io.github.alexisTrejo11.construction.company.modules.project.shared.policy.ProjectAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetProjectPhasesHandler {
  private final ProjectAuthorizationPolicy authorizationPolicy;

  public Result<List<ProjectPhaseResponse>> execute(
      UserContext user,
      GetProjectPhasesQuery query) {
    Result<Void> authorization = authorizationPolicy.requireProjectPermission(
        user,
        query.projectId(),
        Permission.PHASE_READ
    );
    if (!authorization.isSuccess()) {
      return Result.forbidden(authorization.getErrorMessage());
    }

    throw new UnsupportedOperationException("Not supported yet.");
  }
}
