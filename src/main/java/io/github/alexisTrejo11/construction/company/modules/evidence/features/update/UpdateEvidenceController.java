package io.github.alexisTrejo11.construction.company.modules.evidence.features.update;

import io.github.alexisTrejo11.construction.company.modules.evidence.features.evidence.EvidenceCommand;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UpdateEvidenceController {
    private final UpdateEvidenceHandler handler;

    @PatchMapping("/v2/api/evidence/{evidenceId}")
    public ResponseEntity<ResponseWrapper<?>> update(@CurrentUser UserContext user, @PathVariable Long evidenceId, @Valid @RequestBody EvidenceCommand command) {
        var result = handler.handle(user, evidenceId, command);
        if (!result.isSuccess()) return AppErrorResolver.handleResult(result);
        return ResponseEntity.ok(ResponseWrapper.success(result.getData(), "Evidence updated successfully"));
    }
}
