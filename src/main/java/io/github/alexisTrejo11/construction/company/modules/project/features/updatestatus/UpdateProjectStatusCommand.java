package io.github.alexisTrejo11.construction.company.modules.project.features.updatestatus;

import io.github.alexisTrejo11.construction.company.modules.project.shared.domain.ProjectStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateProjectStatusCommand(
    @NotNull(message = "Status is required")
    ProjectStatus status
) {
}
