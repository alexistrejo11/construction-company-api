package io.github.alexisTrejo11.construction.company.modules.budget.features.item.delete;

import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.CurrentUser;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/api/budgets/{budgetId}/items")
@RequiredArgsConstructor
public class DeleteBudgetItemController {
    private final DeleteBudgetItemHandler handler;

    @DeleteMapping("/{itemId}")
    public ResponseEntity<ResponseWrapper<?>> handle(@CurrentUser UserContext user, @PathVariable Long itemId) {
        var result = handler.handle(user, itemId);
        if (!result.isSuccess()) return AppErrorResolver.handleResult(result);
        return ResponseEntity.noContent().build();
    }
}
