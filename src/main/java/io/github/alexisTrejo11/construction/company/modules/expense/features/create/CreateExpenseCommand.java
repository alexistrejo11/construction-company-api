package io.github.alexisTrejo11.construction.company.modules.expense.features.create;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateExpenseCommand(
    @NotNull Long budgetItemId,
    @NotNull BigDecimal amount,
    @NotNull String currency,
    String description
) {
}
