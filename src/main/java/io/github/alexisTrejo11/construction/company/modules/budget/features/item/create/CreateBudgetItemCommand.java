package io.github.alexisTrejo11.construction.company.modules.budget.features.item.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateBudgetItemCommand(
    @NotBlank String description,
    String category,
    String unit,
    Long phaseId,
    @NotNull BigDecimal plannedQuantity,
    @NotNull BigDecimal unitPrice
) {
}
