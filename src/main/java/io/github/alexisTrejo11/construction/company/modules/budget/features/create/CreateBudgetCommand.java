package io.github.alexisTrejo11.construction.company.modules.budget.features.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateBudgetCommand(
    @NotBlank
    @Pattern(regexp = "[A-Z]{3}")
    String currency
) {
}
