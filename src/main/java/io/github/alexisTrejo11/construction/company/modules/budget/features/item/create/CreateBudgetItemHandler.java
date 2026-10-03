package io.github.alexisTrejo11.construction.company.modules.budget.features.item.create;

import io.github.alexisTrejo11.construction.company.modules.budget.shared.domain.BudgetItem;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.domain.BudgetStatus;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.dto.BudgetItemResponse;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.mapper.BudgetMapper;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.persistence.BudgetItemRepository;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.persistence.BudgetRepository;
import io.github.alexisTrejo11.construction.company.modules.project.phases.shared.persistence.ProjectPhaseRepository;
import io.github.alexisTrejo11.construction.company.modules.project.shared.policy.ProjectAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateBudgetItemHandler {
    private final BudgetRepository budgetRepository;
    private final BudgetItemRepository itemRepository;
    private final ProjectPhaseRepository phaseRepository;
    private final ProjectAuthorizationPolicy authorizationPolicy;
    private final BudgetMapper mapper;

    @Transactional
    public Result<BudgetItemResponse> handle(UserContext user, Long budgetId, CreateBudgetItemCommand command) {
        var found = budgetRepository.findById(budgetId);
        if (found.isEmpty()) return Result.notFound("Budget was not found");
        var budget = found.get();
        Result<Void> auth = authorizationPolicy.requireProjectPermission(user, budget.getProject().getId(), Permission.BUDGET_ITEM_CREATE);
        if (!auth.isSuccess()) return Result.error(auth.getErrorType(), auth.getErrorMessage());
        if (budget.getStatus() != BudgetStatus.DRAFT) return Result.business("Only draft budgets can change items");
        var item = new BudgetItem();
        item.setBudget(budget);
        Result<Void> assigned = assign(item, command.description(), command.category(), command.unit(), command.phaseId(), command.plannedQuantity(), command.unitPrice());
        if (!assigned.isSuccess()) return Result.error(assigned.getErrorType(), assigned.getErrorMessage());
        return Result.success(mapper.toResponse(itemRepository.save(item)));
    }

    private Result<Void> assign(BudgetItem item, String description, String category, String unit, Long phaseId, java.math.BigDecimal quantity, java.math.BigDecimal price) {
        if (phaseId != null) {
            var phase = phaseRepository.findByIdAndProjectId(phaseId, item.getBudget().getProject().getId());
            if (phase.isEmpty()) return Result.notFound("Project phase was not found");
            item.setPhase(phase.get());
        } else item.setPhase(null);
        item.setDescription(description);
        item.setCategory(category);
        item.setUnit(unit);
        item.setPlannedQuantity(quantity);
        item.setUnitPrice(price);
        return Result.success();
    }
}
