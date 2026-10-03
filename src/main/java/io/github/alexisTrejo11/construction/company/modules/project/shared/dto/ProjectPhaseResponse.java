package io.github.alexisTrejo11.construction.company.modules.project.shared.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import io.github.alexisTrejo11.construction.company.modules.project.phases.shared.domain.ProjectPhaseStatus;

public record ProjectPhaseResponse(
    Long id,
    String name,
    ProjectPhaseStatus status,
    Integer sequenceOrder,
    BigDecimal allocatedBudget,
    LocalDate startDate,
    LocalDate endDate
) {
}
