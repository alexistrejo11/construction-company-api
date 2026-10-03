package io.github.alexisTrejo11.construction.company.modules.user.features.createinvitation;

import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record CreateInvitationCommand(
    @NotBlank @Email String email,
    @NotEmpty Set<UserRole> roles
) {
}
