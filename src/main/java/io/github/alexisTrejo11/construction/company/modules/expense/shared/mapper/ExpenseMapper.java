package io.github.alexisTrejo11.construction.company.modules.expense.shared.mapper;

import io.github.alexisTrejo11.construction.company.modules.expense.shared.domain.Expense;
import io.github.alexisTrejo11.construction.company.modules.expense.shared.dto.ExpenseResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ExpenseMapper {
    @Mapping(target = "budgetId", source = "budget.id")
    @Mapping(target = "budgetItemId", source = "budgetItem.id")
    ExpenseResponse toResponse(Expense expense);
}
