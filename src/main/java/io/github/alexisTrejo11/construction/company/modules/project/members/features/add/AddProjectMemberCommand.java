package io.github.alexisTrejo11.construction.company.modules.project.members.features.add;

import jakarta.validation.constraints.NotNull;

public record AddProjectMemberCommand(
    @NotNull(message = "User ID is required")
    Long userId
) {
}
