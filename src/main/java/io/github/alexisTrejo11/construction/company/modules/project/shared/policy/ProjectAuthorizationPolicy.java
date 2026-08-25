package io.github.alexisTrejo11.construction.company.modules.project.shared.policy;

import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;

public interface ProjectAuthorizationPolicy {
    Result<Void> requireProjectPermission(
        UserContext user,
        Long projectId,
        Permission permission
    );
}
