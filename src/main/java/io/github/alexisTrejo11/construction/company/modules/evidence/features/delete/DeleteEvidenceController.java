package io.github.alexisTrejo11.construction.company.modules.evidence.features.delete;

import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.CurrentUser;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DeleteEvidenceController {
    private final DeleteEvidenceHandler handler;

    @DeleteMapping("/v2/api/evidence/{evidenceId}")
    public ResponseEntity<ResponseWrapper<?>> delete(@CurrentUser UserContext user, @PathVariable Long evidenceId) {
        var result = handler.handle(user, evidenceId);
        if (!result.isSuccess()) return AppErrorResolver.handleResult(result);
        return ResponseEntity.noContent().build();
    }
}
