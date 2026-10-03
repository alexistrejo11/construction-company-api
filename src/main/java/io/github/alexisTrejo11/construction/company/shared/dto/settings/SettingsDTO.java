package io.github.alexisTrejo11.construction.company.shared.dto.settings;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettingsDTO {

    @JsonProperty("max_expense_limit")
    @NotNull(message = "max_expense_limit is obligatory")
    @Positive(message = "max_expense_limit must be positive")
    private Double maxExpenseLimit;

    @JsonProperty("allowed_categories")
    @NotNull(message = "allowed_categories is obligatory")
    @Size(min = 1, message = "allowed_categories must be positive")
    private List<String> allowedCategories;
}
