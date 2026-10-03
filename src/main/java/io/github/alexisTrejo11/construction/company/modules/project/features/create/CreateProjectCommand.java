package io.github.alexisTrejo11.construction.company.modules.project.features.create;

import io.github.alexisTrejo11.construction.company.modules.project.shared.dto.SiteLocationRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateProjectCommand(
    @NotBlank(message = "Project name is required")
    @Size(max = 150, message = "Project name must not exceed 150 characters")
    String name,

    @NotBlank(message = "Project code is required")
    @Size(max = 30, message = "Project code must not exceed 30 characters")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Project code must be alphanumeric and upper case (dashes/underscores allowed)")
    String code,

    String description,

    @NotNull(message = "Total budget is required")
    @PositiveOrZero(message = "Total budget must be zero or positive")
    BigDecimal totalBudget,

    @Valid
    SiteLocationRequest location,

    LocalDate startDate,

    LocalDate estimatedEndDate
) {
}
