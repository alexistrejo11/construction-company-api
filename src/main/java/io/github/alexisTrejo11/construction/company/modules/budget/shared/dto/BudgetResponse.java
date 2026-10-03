package io.github.alexisTrejo11.construction.company.modules.budget.shared.dto;

import io.github.alexisTrejo11.construction.company.modules.budget.shared.domain.BudgetStatus;

public record BudgetResponse(Long id, Long projectId, String currency, BudgetStatus status) {
}
