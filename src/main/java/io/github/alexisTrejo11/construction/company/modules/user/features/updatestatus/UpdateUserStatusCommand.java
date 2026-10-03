package io.github.alexisTrejo11.construction.company.modules.user.features.updatestatus;

import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusCommand(
    @NotNull UserStatus status
) {
}
