package io.github.alexisTrejo11.construction.company.modules.budget.features.item.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateBudgetItemCommand(
    @NotBlank String description,
    String category,
    String unit,
    Long phaseId,
    @NotNull BigDecimal plannedQuantity,
    @NotNull BigDecimal unitPrice
) {
}
