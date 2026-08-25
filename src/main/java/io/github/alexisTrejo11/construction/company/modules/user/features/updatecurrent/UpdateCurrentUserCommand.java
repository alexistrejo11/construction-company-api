package io.github.alexisTrejo11.construction.company.modules.user.features.updatecurrent;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCurrentUserCommand(
    @NotBlank @Size(max = 100) String firstName,
    @NotBlank @Size(max = 100) String lastName,
    @Size(max = 20) String phone
) {
}
