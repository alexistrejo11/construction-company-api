package io.github.alexisTrejo11.construction.company.modules.budget.shared.dto;

import java.math.BigDecimal;

public record BudgetItemResponse(
    Long id,
    Long budgetId,
    Long phaseId,
    String description,
    String category,
    String unit,
    BigDecimal plannedQuantity,
    BigDecimal unitPrice,
    BigDecimal plannedTotal
) {
}
