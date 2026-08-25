package io.github.alexisTrejo11.construction.company.modules.project.members.features.getbyuser;

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
@RequestMapping("/v2/api/projects/{projectId}/members")
@RequiredArgsConstructor
public class GetProjectMemberController {
    private final GetProjectMemberHandler handler;

    @GetMapping("/{userId}")
    public ResponseEntity<ResponseWrapper<?>> get(
        @CurrentUser UserContext actor,
        @PathVariable Long projectId,
        @PathVariable Long userId
    ) {
        var result = handler.execute(actor, projectId, userId);
        if (!result.isSuccess()) {
            return AppErrorResolver.handleResult(result);
        }

        return ResponseEntity.ok(ResponseWrapper.found(result.getData(), "Project member"));
    }
}
