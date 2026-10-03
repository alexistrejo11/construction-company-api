package io.github.alexisTrejo11.construction.company.shared.dto.auth;

import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.UserRole;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import java.util.Set;

public record UserContext(
    Long userId,
    String email,
    Set<UserRole> roles,
    Set<Permission> permissions
) {
    public UserContext {
        roles = Set.copyOf(roles);
        permissions = Set.copyOf(permissions);
    }
}
