package io.github.alexisTrejo11.construction.company.modules.budget.features.item.get;

import io.github.alexisTrejo11.construction.company.modules.budget.shared.dto.BudgetItemResponse;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.mapper.BudgetMapper;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.persistence.BudgetItemRepository;
import io.github.alexisTrejo11.construction.company.modules.project.shared.policy.ProjectAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetBudgetItemHandler {
    private final BudgetItemRepository itemRepository;
    private final ProjectAuthorizationPolicy authorizationPolicy;
    private final BudgetMapper mapper;

    @Transactional(readOnly = true)
    public Result<BudgetItemResponse> handle(UserContext user, Long itemId) {
        var found = itemRepository.findById(itemId);
        if (found.isEmpty()) return Result.notFound("Budget item was not found");
        Result<Void> auth = authorizationPolicy.requireProjectPermission(user, found.get().getBudget().getProject().getId(), Permission.BUDGET_ITEM_READ);
        if (!auth.isSuccess()) return Result.error(auth.getErrorType(), auth.getErrorMessage());
        return Result.success(mapper.toResponse(found.get()));
    }
}
