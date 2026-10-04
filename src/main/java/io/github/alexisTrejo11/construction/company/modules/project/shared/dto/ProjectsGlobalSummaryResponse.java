package io.github.alexisTrejo11.construction.company.modules.project.shared.dto;

import java.math.BigDecimal;

public record ProjectsGlobalSummaryResponse(
    Long totalProjects,
    Long planningCount,
    Long inProgressCount,
    Long onHoldCount,
    Long completedCount,
    Long cancelledCount,
    BigDecimal totalBudget
) {
}
