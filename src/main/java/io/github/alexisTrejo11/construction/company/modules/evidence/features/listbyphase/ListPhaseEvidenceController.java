package io.github.alexisTrejo11.construction.company.modules.evidence.features.listbyphase;

import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.CurrentUser;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ListPhaseEvidenceController {
    private final ListPhaseEvidenceHandler handler;

    @GetMapping("/v2/api/projects/{projectId}/phases/{phaseId}/evidence")
    public ResponseEntity<ResponseWrapper<?>> list(@CurrentUser UserContext user, @PathVariable Long projectId, @PathVariable Long phaseId) {
        var result = handler.handle(user, projectId, phaseId);
        if (!result.isSuccess()) {
            return AppErrorResolver.handleResult(result);
        }
        return ResponseEntity.ok(ResponseWrapper.found(result.getData(), "Evidence"));
    }
}
