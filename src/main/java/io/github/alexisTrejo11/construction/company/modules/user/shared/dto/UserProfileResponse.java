package io.github.alexisTrejo11.construction.company.modules.user.shared.dto;

import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.UserRole;
import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.UserStatus;
import java.util.Set;

public record UserProfileResponse(
    Long id,
    String email,
    String firstName,
    String lastName,
    String phone,
    UserStatus status,
    Set<UserRole> roles
) {
}
