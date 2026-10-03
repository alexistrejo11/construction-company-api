package io.github.alexisTrejo11.construction.company.modules.project.features.get;

import io.github.alexisTrejo11.construction.company.modules.project.shared.domain.ProjectStatus;
import io.github.alexisTrejo11.construction.company.shared.dto.PageRequest;
import jakarta.validation.constraints.Size;

public record GetProjectQuery(
    @Size(min = 3, max = 255)
    String search,
    ProjectStatus status,
    @Size(min = 3, max = 255)
    String city,
    PageRequest pageRequest
) {
}
