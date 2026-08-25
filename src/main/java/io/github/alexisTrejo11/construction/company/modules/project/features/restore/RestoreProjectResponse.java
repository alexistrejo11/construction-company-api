package io.github.alexisTrejo11.construction.company.modules.project.features.restore;

import io.github.alexisTrejo11.construction.company.modules.project.shared.domain.ProjectStatus;

public record RestoreProjectResponse(
    Long projectId,
    ProjectStatus status
) {
}
