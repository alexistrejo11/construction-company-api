package io.github.alexisTrejo11.construction.company.modules.budget.features.getbyproject;

import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.CurrentUser;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/api")
@RequiredArgsConstructor
public class GetBudgetByProjectController {
    private final GetBudgetByProjectHandler handler;

    @GetMapping("/projects/{projectId}/budget")
    public ResponseEntity<ResponseWrapper<?>> handle(@CurrentUser UserContext user, @PathVariable Long projectId) {
        var result = handler.handle(user, projectId);
        if (!result.isSuccess()) return AppErrorResolver.handleResult(result);
        return ResponseEntity.ok(ResponseWrapper.success(result.getData(), "Budget fetched successfully"));
    }
}
