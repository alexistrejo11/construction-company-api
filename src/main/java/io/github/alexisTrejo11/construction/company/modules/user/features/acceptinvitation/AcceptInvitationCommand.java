package io.github.alexisTrejo11.construction.company.modules.user.features.acceptinvitation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcceptInvitationCommand(
    @NotBlank @Size(min = 12, max = 72) String password,
    @NotBlank @Size(max = 100) String firstName,
    @NotBlank @Size(max = 100) String lastName,
    @Size(max = 20) String phone
) {
}
