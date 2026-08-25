package io.github.alexisTrejo11.construction.company.modules.project.features.restore;

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
@RequestMapping("/v2/api/projects")
@RequiredArgsConstructor
public class RestoreProjectController {
    private final RestoreProjectHandler handler;

    @PostMapping("/{projectId}/restore")
    public ResponseEntity<ResponseWrapper<?>> restore(
        @CurrentUser UserContext user,
        @PathVariable Long projectId
    ) {
        var result = handler.execute(user, projectId);
        if (!result.isSuccess()) {
            return AppErrorResolver.handleResult(result);
        }

        return ResponseEntity.ok(ResponseWrapper.success(result.getData(), "Project restored successfully"));
    }
}
