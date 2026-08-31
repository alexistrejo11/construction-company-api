package io.github.alexisTrejo11.construction.company.modules.expense.shared.dto;

import io.github.alexisTrejo11.construction.company.modules.expense.shared.domain.ExpenseStatus;
import java.math.BigDecimal;

public record ExpenseResponse(
    Long id,
    Long budgetId,
    Long budgetItemId,
    BigDecimal amount,
    String currency,
    String description,
    ExpenseStatus status
) {
}
