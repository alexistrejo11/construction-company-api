package io.github.alexisTrejo11.construction.company.modules.project.shared.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ProjectResponse(
    Long id,
    String name,
    String code,
    String description,
    String status,
    BigDecimal totalBudget,
    SiteLocationResponse location,
    LocalDate startDate,
    LocalDate estimatedEndDate,
    LocalDate actualEndDate,
    List<ProjectPhaseResponse> phases,
    List<ProjectMemberResponse> members,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
