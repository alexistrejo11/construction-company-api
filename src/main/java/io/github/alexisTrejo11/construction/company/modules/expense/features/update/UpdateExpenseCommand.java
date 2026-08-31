package io.github.alexisTrejo11.construction.company.modules.expense.features.update;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateExpenseCommand(
    @NotNull Long budgetItemId,
    @NotNull BigDecimal amount,
    @NotNull String currency,
    String description
) {
}
