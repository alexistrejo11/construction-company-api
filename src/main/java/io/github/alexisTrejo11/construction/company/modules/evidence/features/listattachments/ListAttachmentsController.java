package io.github.alexisTrejo11.construction.company.modules.evidence.features.listattachments;

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
public class ListAttachmentsController {
    private final ListAttachmentsHandler handler;

    @GetMapping("/v2/api/evidence/{evidenceId}/attachments")
    public ResponseEntity<ResponseWrapper<?>> list(@CurrentUser UserContext user, @PathVariable Long evidenceId) {
        var result = handler.handle(user, evidenceId);
        if (!result.isSuccess()) return AppErrorResolver.handleResult(result);
        return ResponseEntity.ok(ResponseWrapper.found(result.getData(), "Attachment"));
    }
}
