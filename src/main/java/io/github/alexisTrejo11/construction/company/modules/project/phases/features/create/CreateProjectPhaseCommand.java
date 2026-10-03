package io.github.alexisTrejo11.construction.company.modules.project.phases.features.create;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateProjectPhaseCommand(
    @NotBlank(message = "Phase name is required")
    @Size(max = 100, message = "Phase name must not exceed 100 characters")
    String name,

    @NotNull(message = "Sequence order is required")
    @Min(value = 1, message = "Sequence order must be at least 1")
    Integer sequenceOrder,

    @NotNull(message = "Allocated budget is required")
    @PositiveOrZero(message = "Allocated budget must be zero or positive")
    BigDecimal allocatedBudget,

    LocalDate startDate,

    LocalDate endDate
) {
}
