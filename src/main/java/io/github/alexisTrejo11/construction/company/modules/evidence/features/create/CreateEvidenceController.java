package io.github.alexisTrejo11.construction.company.modules.evidence.features.create;

import io.github.alexisTrejo11.construction.company.modules.evidence.features.evidence.EvidenceCommand;
import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.CurrentUser;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CreateEvidenceController {
    private final CreateEvidenceHandler handler;

    @PostMapping("/v2/api/projects/{projectId}/phases/{phaseId}/evidence")
    public ResponseEntity<ResponseWrapper<?>> create(
        @CurrentUser UserContext user,
        @PathVariable Long projectId,
        @PathVariable Long phaseId,
        @Valid @RequestBody EvidenceCommand command
    ) {
        var result = handler.handle(user, projectId, phaseId, command);
        if (!result.isSuccess()) {
            return AppErrorResolver.handleResult(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.created(result.getData(), "Evidence"));
    }
}
