package io.github.alexisTrejo11.construction.company.modules.project.shared.dto;

import java.math.BigDecimal;

public record ProjectsGlobalSummaryResponse(
    long totalProjects,
    long planningCount,
    long inProgressCount,
    long onHoldCount,
    long completedCount,
    long cancelledCount,
    BigDecimal totalBudget
) {
}
