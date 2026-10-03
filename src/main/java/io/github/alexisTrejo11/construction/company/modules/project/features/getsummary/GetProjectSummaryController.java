package io.github.alexisTrejo11.construction.company.modules.project.features.getsummary;

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
@RequestMapping("/v2/api/projects")
@RequiredArgsConstructor
public class GetProjectSummaryController {
    private final GetProjectSummaryHandler handler;

    @GetMapping("/{projectId}/summary")
    public ResponseEntity<ResponseWrapper<?>> getSummary(
        @CurrentUser UserContext user,
        @PathVariable Long projectId
    ) {
        var result = handler.execute(user, projectId);
        if (!result.isSuccess()) {
            return AppErrorResolver.handleResult(result);
        }

        return ResponseEntity.ok(ResponseWrapper.found(result.getData(), "Project summary"));
    }
}
