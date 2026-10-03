package io.github.alexisTrejo11.construction.company.modules.project.features.getmyprojects;

import io.github.alexisTrejo11.construction.company.shared.dto.PageRequest;

public record GetMyProjectsQuery(Long userId, PageRequest pageRequest) {
}
