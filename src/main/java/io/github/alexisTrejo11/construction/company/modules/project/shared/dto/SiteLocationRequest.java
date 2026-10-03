package io.github.alexisTrejo11.construction.company.modules.project.shared.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SiteLocationRequest(
    @Size(max = 255, message = "Address line must not exceed 255 characters")
    String addressLine,

    @Size(max = 100, message = "City must not exceed 100 characters")
    String city,

    @Size(max = 100, message = "State must not exceed 100 characters")
    String state,

    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    String postalCode,

    @Size(max = 100, message = "Country must not exceed 100 characters")
    String country,

    @DecimalMin(value = "-90.0", message = "Latitude must be greater than or equal to -90.0")
    @DecimalMax(value = "90.0", message = "Latitude must be less than or equal to 90.0")
    BigDecimal latitude,

    @DecimalMin(value = "-180.0", message = "Longitude must be greater than or equal to -180.0")
    @DecimalMax(value = "180.0", message = "Longitude must be less than or equal to 180.0")
    BigDecimal longitude
) {
}
