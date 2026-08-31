package io.github.alexisTrejo11.construction.company.modules.budget.features.item.update;

import io.github.alexisTrejo11.construction.company.modules.budget.shared.domain.BudgetItem;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.domain.BudgetStatus;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.dto.BudgetItemResponse;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.mapper.BudgetMapper;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.persistence.BudgetItemRepository;
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
public class UpdateBudgetItemHandler {
    private final BudgetItemRepository itemRepository;
    private final ProjectPhaseRepository phaseRepository;
    private final ProjectAuthorizationPolicy authorizationPolicy;
    private final BudgetMapper mapper;

    @Transactional
    public Result<BudgetItemResponse> handle(UserContext user, Long itemId, UpdateBudgetItemCommand command) {
        var found = itemRepository.findById(itemId);
        if (found.isEmpty()) return Result.notFound("Budget item was not found");
        var item = found.get();
        Result<Void> auth = authorizationPolicy.requireProjectPermission(user, item.getBudget().getProject().getId(), Permission.BUDGET_ITEM_UPDATE);
        if (!auth.isSuccess()) return Result.error(auth.getErrorType(), auth.getErrorMessage());
        if (item.getBudget().getStatus() != BudgetStatus.DRAFT) return Result.business("Only draft budgets can change items");
        if (command.phaseId() != null) {
            var phase = phaseRepository.findByIdAndProjectId(command.phaseId(), item.getBudget().getProject().getId());
            if (phase.isEmpty()) return Result.notFound("Project phase was not found");
            item.setPhase(phase.get());
        } else item.setPhase(null);
        item.setDescription(command.description()); item.setCategory(command.category()); item.setUnit(command.unit());
        item.setPlannedQuantity(command.plannedQuantity()); item.setUnitPrice(command.unitPrice());
        return Result.success(mapper.toResponse(itemRepository.save(item)));
    }
}
