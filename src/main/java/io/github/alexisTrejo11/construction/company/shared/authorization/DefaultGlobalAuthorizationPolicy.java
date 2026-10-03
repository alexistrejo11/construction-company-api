package io.github.alexisTrejo11.construction.company.shared.authorization;

import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import org.springframework.stereotype.Component;

@Component
public class DefaultGlobalAuthorizationPolicy implements GlobalAuthorizationPolicy {
    @Override
    public Result<Void> requirePermission(UserContext user, Permission permission) {
        if (user == null || user.userId() == null) {
            return Result.unauthenticated("Authenticated user was not found");
        }

        if (user.permissions().contains(permission)) {
            return Result.success();
        }

        return Result.forbidden("You do not have permission to perform this operation");
    }
}
