package io.github.alexisTrejo11.construction.company.modules.project.phases.features.status;

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
@RequestMapping("/v2/api/projects/{projectId}/phases")
@RequiredArgsConstructor
public class UpdateProjectPhaseStatusController {
    private final UpdateProjectPhaseStatusHandler handler;

    @PatchMapping("/{phaseId}/status")
    public ResponseEntity<ResponseWrapper<?>> update(
        @CurrentUser UserContext actor,
        @PathVariable Long projectId,
        @PathVariable Long phaseId,
        @Valid @RequestBody UpdateProjectPhaseStatusCommand command
    ) {
        var result = handler.execute(actor, projectId, phaseId, command);
        if (!result.isSuccess()) {
            return AppErrorResolver.handleResult(result);
        }

        return ResponseEntity.ok(ResponseWrapper.success(result.getData(), "Project phase status updated successfully"));
    }
}
