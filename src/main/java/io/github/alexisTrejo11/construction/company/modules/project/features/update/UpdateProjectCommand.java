package io.github.alexisTrejo11.construction.company.modules.project.features.update;

import io.github.alexisTrejo11.construction.company.modules.project.shared.dto.SiteLocationRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateProjectCommand(
    @Size(max = 150, message = "Project name must not exceed 150 characters")
    String name,

    String description,

    @PositiveOrZero(message = "Total budget must be zero or positive")
    BigDecimal totalBudget,

    @Valid
    SiteLocationRequest location,

    LocalDate startDate,

    LocalDate estimatedEndDate,

    LocalDate actualEndDate
) {
}
