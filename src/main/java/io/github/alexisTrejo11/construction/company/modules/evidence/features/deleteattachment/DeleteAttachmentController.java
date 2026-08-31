package io.github.alexisTrejo11.construction.company.modules.evidence.features.deleteattachment;

import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.CurrentUser;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DeleteAttachmentController {
    private final DeleteAttachmentHandler handler;

    @DeleteMapping("/v2/api/attachments/{attachmentId}")
    public ResponseEntity<?> delete(@CurrentUser UserContext user, @PathVariable Long attachmentId) {
        var result = handler.handle(user, attachmentId);
        if (!result.isSuccess()) return AppErrorResolver.handleResult(result);
        return ResponseEntity.noContent().build();
    }
}
