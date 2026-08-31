package io.github.alexisTrejo11.construction.company.modules.budget.shared.mapper;

import io.github.alexisTrejo11.construction.company.modules.budget.shared.domain.Budget;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.domain.BudgetItem;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.dto.BudgetItemResponse;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.dto.BudgetResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface BudgetMapper {
    @Mapping(target = "projectId", source = "project.id")
    BudgetResponse toResponse(Budget budget);

    @Mapping(target = "budgetId", source = "budget.id")
    @Mapping(target = "phaseId", source = "phase.id")
    @Mapping(target = "plannedTotal", expression = "java(item.plannedTotal())")
    BudgetItemResponse toResponse(BudgetItem item);
}
