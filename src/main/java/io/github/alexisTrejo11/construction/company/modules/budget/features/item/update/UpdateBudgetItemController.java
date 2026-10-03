package io.github.alexisTrejo11.construction.company.modules.budget.features.item.update;

import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.CurrentUser;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/api/budgets/{budgetId}/items")
@RequiredArgsConstructor
public class UpdateBudgetItemController {
    private final UpdateBudgetItemHandler handler;

    @PatchMapping("/{itemId}")
    public ResponseEntity<ResponseWrapper<?>> handle(@CurrentUser UserContext user, @PathVariable Long itemId,
        @Valid @RequestBody UpdateBudgetItemCommand command) {
        var result = handler.handle(user, itemId, command);
        if (!result.isSuccess()) return AppErrorResolver.handleResult(result);
        return ResponseEntity.ok(ResponseWrapper.success(result.getData(), "BudgetItem updated successfully"));
    }
}
