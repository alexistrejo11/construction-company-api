package io.github.alexisTrejo11.construction.company.modules.budget.shared.dto;

import java.math.BigDecimal;

public record BudgetSummaryResponse(
    Long budgetId,
    String currency,
    BigDecimal plannedAmount,
    BigDecimal executedAmount,
    BigDecimal balance,
    BigDecimal variance,
    boolean overBudget
) {
}
