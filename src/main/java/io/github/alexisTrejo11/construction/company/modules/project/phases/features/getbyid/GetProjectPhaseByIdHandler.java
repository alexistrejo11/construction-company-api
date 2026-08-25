package io.github.alexisTrejo11.construction.company.modules.project.phases.features.getbyid;

import io.github.alexisTrejo11.construction.company.modules.project.shared.dto.ProjectPhaseResponse;
import io.github.alexisTrejo11.construction.company.modules.project.shared.policy.ProjectAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetProjectPhaseByIdHandler {
  private final ProjectAuthorizationPolicy authorizationPolicy;

  public Result<ProjectPhaseResponse> execute(
      UserContext user,
      GetProjectPhaseByIdQuery query) {
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
