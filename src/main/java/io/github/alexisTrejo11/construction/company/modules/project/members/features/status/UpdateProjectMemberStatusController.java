package io.github.alexisTrejo11.construction.company.modules.project.members.features.status;

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
@RequestMapping("/v2/api/projects/{projectId}/members")
@RequiredArgsConstructor
public class UpdateProjectMemberStatusController {
    private final UpdateProjectMemberStatusHandler handler;

    @PatchMapping("/{userId}/status")
    public ResponseEntity<ResponseWrapper<?>> update(
        @CurrentUser UserContext actor,
        @PathVariable Long projectId,
        @PathVariable Long userId,
        @Valid @RequestBody UpdateProjectMemberStatusCommand command
    ) {
        var result = handler.execute(actor, projectId, userId, command);
        if (!result.isSuccess()) {
            return AppErrorResolver.handleResult(result);
        }

        return ResponseEntity.ok(ResponseWrapper.success(result.getData(), "Project member status updated successfully"));
    }
}
