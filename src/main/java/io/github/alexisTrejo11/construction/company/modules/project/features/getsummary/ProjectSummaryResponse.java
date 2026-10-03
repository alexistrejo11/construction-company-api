package io.github.alexisTrejo11.construction.company.modules.project.features.getsummary;

import io.github.alexisTrejo11.construction.company.modules.project.shared.domain.ProjectStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjectSummaryResponse(
    Long projectId,
    String name,
    String code,
    ProjectStatus status,
    BigDecimal totalBudget,
    LocalDate startDate,
    LocalDate estimatedEndDate,
    LocalDate actualEndDate
) {
}
