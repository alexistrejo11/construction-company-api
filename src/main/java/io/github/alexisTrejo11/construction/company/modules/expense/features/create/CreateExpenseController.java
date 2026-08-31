package io.github.alexisTrejo11.construction.company.modules.expense.features.create;

import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.CurrentUser;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/api")
@RequiredArgsConstructor
public class CreateExpenseController {
    private final CreateExpenseHandler handler;

    @PostMapping("/budgets/{budgetId}/expenses")
    public ResponseEntity<ResponseWrapper<?>> handle(@CurrentUser UserContext user, @PathVariable Long budgetId, @Valid @RequestBody CreateExpenseCommand command) {
        var result = handler.handle(user, budgetId, command);
        if (!result.isSuccess()) return AppErrorResolver.handleResult(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.created(result.getData(), "Expense"));
    }
}
