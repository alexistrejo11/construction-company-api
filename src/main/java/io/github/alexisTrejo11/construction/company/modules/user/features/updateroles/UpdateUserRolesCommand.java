package io.github.alexisTrejo11.construction.company.modules.user.features.updateroles;

import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.UserRole;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record UpdateUserRolesCommand(
    @NotEmpty Set<UserRole> roles
) {
}
