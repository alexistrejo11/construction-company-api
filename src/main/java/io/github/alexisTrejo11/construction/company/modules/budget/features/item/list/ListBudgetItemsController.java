package io.github.alexisTrejo11.construction.company.modules.budget.features.item.list;

import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.CurrentUser;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/api/budgets/{budgetId}/items")
@RequiredArgsConstructor
public class ListBudgetItemsController {
    private final ListBudgetItemsHandler handler;

    @GetMapping
    public ResponseEntity<ResponseWrapper<?>> handle(@CurrentUser UserContext user, @PathVariable Long budgetId,
        @RequestParam(required = false) String search, @RequestParam(required = false) String category,
        @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        var result = handler.handle(user, budgetId, search, category, page, size);
        if (!result.isSuccess()) return AppErrorResolver.handleResult(result);
        return ResponseEntity.ok(ResponseWrapper.found(result.getData(), "BudgetItem"));
    }
}
