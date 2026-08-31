package io.github.alexisTrejo11.construction.company.modules.expense.features.list;

import io.github.alexisTrejo11.construction.company.modules.expense.shared.domain.ExpenseStatus;
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
@RequestMapping("/v2/api")
@RequiredArgsConstructor
public class ListExpensesController {
    private final ListExpensesHandler handler;

    @GetMapping("/budgets/{budgetId}/expenses")
    public ResponseEntity<ResponseWrapper<?>> handle(@CurrentUser UserContext user, @PathVariable Long budgetId,
        @RequestParam(required = false) String search, @RequestParam(required = false) ExpenseStatus status,
        @RequestParam(required = false) String currency, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        var result = handler.handle(user, budgetId, search, status, currency, page, size);
        if (!result.isSuccess()) return AppErrorResolver.handleResult(result);
        return ResponseEntity.ok(ResponseWrapper.found(result.getData(), "Expense"));
    }
}
