package io.github.alexisTrejo11.construction.company.modules.project.members.features.status;

import io.github.alexisTrejo11.construction.company.modules.project.members.shared.domain.ProjectMemberStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateProjectMemberStatusCommand(
    @NotNull ProjectMemberStatus status
) {
}
