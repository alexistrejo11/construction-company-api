package io.github.alexisTrejo11.construction.company.modules.budget.features.item.list;

import io.github.alexisTrejo11.construction.company.modules.budget.shared.dto.BudgetItemResponse;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.persistence.BudgetItemRepository;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.persistence.BudgetRepository;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.mapper.BudgetMapper;
import io.github.alexisTrejo11.construction.company.modules.project.shared.policy.ProjectAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.PageResponse;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListBudgetItemsHandler {
    private final BudgetRepository budgetRepository;
    private final BudgetItemRepository itemRepository;
    private final ProjectAuthorizationPolicy authorizationPolicy;
    private final BudgetMapper mapper;

    @Transactional(readOnly = true)
    public Result<PageResponse<BudgetItemResponse>> handle(UserContext user, Long budgetId, String search, String category, int page, int size) {
        var budget = budgetRepository.findById(budgetId);
        if (budget.isEmpty()) return Result.notFound("Budget was not found");
        Result<Void> auth = authorizationPolicy.requireProjectPermission(user, budget.get().getProject().getId(), Permission.BUDGET_ITEM_READ);
        if (!auth.isSuccess()) return Result.error(auth.getErrorType(), auth.getErrorMessage());
        if (page < 1 || size < 1 || size > 100) return Result.validation("Invalid pagination parameters");
        var items = itemRepository.findByBudgetIdOrderByCreatedAtAsc(budgetId).stream()
            .filter(item -> search == null || item.getDescription().toLowerCase().contains(search.toLowerCase()))
            .filter(item -> category == null || category.equalsIgnoreCase(item.getCategory())).map(mapper::toResponse).toList();
        int from = Math.min((page - 1) * size, items.size());
        int to = Math.min(from + size, items.size());
        return Result.success(new PageResponse<>(items.subList(from, to), page, size, items.size(), (items.size() + size - 1) / size));
    }
}
