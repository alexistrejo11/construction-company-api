package io.github.alexisTrejo11.construction.company.modules.budget.features.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateBudgetCommand(
    @NotBlank
    @Pattern(regexp = "[A-Z]{3}")
    String currency
) {
}
