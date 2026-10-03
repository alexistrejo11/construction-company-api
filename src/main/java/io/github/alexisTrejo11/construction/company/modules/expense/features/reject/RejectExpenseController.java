package io.github.alexisTrejo11.construction.company.modules.expense.features.reject;

import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.CurrentUser;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/api")
@RequiredArgsConstructor
public class RejectExpenseController {
    private final RejectExpenseHandler handler;

    @PostMapping("/expenses/{expenseId}/reject")
    public ResponseEntity<ResponseWrapper<?>> handle(@CurrentUser UserContext user, @PathVariable Long expenseId) {
        var result = handler.handle(user, expenseId);
        if (!result.isSuccess()) {
            return AppErrorResolver.handleResult(result);
        }
        return ResponseEntity.ok(ResponseWrapper.success(result.getData(), "Expense status updated successfully"));
    }
}
