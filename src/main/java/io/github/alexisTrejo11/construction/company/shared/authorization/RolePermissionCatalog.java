package io.github.alexisTrejo11.construction.company.shared.authorization;

import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.UserRole;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class RolePermissionCatalog {
    private static final Set<Permission> ALL_PERMISSIONS = Collections.unmodifiableSet(
        EnumSet.allOf(Permission.class)
    );

    private static final EnumMap<UserRole, Set<Permission>> PERMISSIONS = permissions();

    public Set<Permission> permissionsFor(UserRole role) {
        return PERMISSIONS.getOrDefault(role, Set.of());
    }

    public Set<Permission> permissionsFor(Set<UserRole> roles) {
        var permissions = EnumSet.noneOf(Permission.class);

        roles.forEach(role -> permissions.addAll(permissionsFor(role)));

        return Collections.unmodifiableSet(permissions);
    }

    private static EnumMap<UserRole, Set<Permission>> permissions() {
        var permissions = new EnumMap<UserRole, Set<Permission>>(UserRole.class);

        for (UserRole role : UserRole.values()) {
            permissions.put(role, Set.of());
        }

        permissions.put(UserRole.COMPANY_ADMIN, ALL_PERMISSIONS);

        return permissions;
    }
}
