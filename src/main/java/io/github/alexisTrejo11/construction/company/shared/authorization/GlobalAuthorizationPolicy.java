package io.github.alexisTrejo11.construction.company.shared.authorization;

import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;

public interface GlobalAuthorizationPolicy {
    Result<Void> requirePermission(UserContext user, Permission permission);
}
