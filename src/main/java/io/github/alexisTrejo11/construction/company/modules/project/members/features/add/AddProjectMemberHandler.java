package io.github.alexisTrejo11.construction.company.modules.project.members.features.add;

import io.github.alexisTrejo11.construction.company.modules.project.shared.dto.ProjectMemberResponse;
import io.github.alexisTrejo11.construction.company.modules.project.shared.policy.ProjectAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AddProjectMemberHandler {
  private final ProjectAuthorizationPolicy authorizationPolicy;

  public Result<ProjectMemberResponse> execute(
      UserContext user,
      Long projectId,
      AddProjectMemberCommand command) {
    Result<Void> authorization = authorizationPolicy.requireProjectPermission(
        user,
        projectId,
        Permission.MEMBER_MANAGE
    );
    if (!authorization.isSuccess()) {
      return Result.forbidden(authorization.getErrorMessage());
    }

    throw new UnsupportedOperationException("Not supported yet.");
  }
}
