package io.github.alexisTrejo11.construction.company.modules.project.phases.features.status;

import io.github.alexisTrejo11.construction.company.modules.project.phases.shared.domain.ProjectPhaseStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateProjectPhaseStatusCommand(
    @NotNull ProjectPhaseStatus status
) {
}
