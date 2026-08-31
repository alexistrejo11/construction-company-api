package io.github.alexisTrejo11.construction.company.modules.evidence.features.createattachment;

import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.CurrentUser;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class CreateAttachmentController {
    private final CreateAttachmentHandler handler;

    @PostMapping(path = "/v2/api/evidence/{evidenceId}/attachments", consumes = "multipart/form-data")
    public ResponseEntity<ResponseWrapper<?>> create(@CurrentUser UserContext user, @PathVariable Long evidenceId, @RequestParam("file") MultipartFile file) {
        var result = handler.handle(user, evidenceId, file);
        if (!result.isSuccess()) return AppErrorResolver.handleResult(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.created(result.getData(), "Attachment"));
    }
}
