package io.github.alexisTrejo11.construction.company.modules.evidence.features.deleteattachment;

import io.github.alexisTrejo11.construction.company.modules.evidence.shared.access.EvidenceAccess;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.persistence.AttachmentRepository;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.storage.FileStorage;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteAttachmentHandler {
    private final EvidenceAccess evidenceAccess;
    private final AttachmentRepository attachmentRepository;
    private final FileStorage fileStorage;

    @Transactional
    public Result<Void> handle(UserContext user, Long attachmentId) {
        var attachment = attachmentRepository.findById(attachmentId);
        if (attachment.isEmpty()) return Result.notFound("Attachment was not found");
        var evidence = evidenceAccess.requireEvidence(user, attachment.get().getEvidence().getId(), Permission.ATTACHMENT_DELETE);
        if (!evidence.isSuccess()) return Result.error(evidence.getErrorType(), evidence.getErrorMessage());
        try {
            fileStorage.delete(attachment.get().getStorageKey());
        } catch (IOException exception) {
            return Result.error("Attachment could not be removed from storage");
        }
        attachmentRepository.delete(attachment.get());
        return Result.success();
    }
}
