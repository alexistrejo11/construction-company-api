package io.github.alexisTrejo11.construction.company.modules.auth.features.login;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginCommand(
    @NotBlank @Email String email,
    @NotBlank String password
) {
}
