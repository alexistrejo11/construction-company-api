package io.github.alexisTrejo11.construction.company.modules.evidence.features.delete;

import io.github.alexisTrejo11.construction.company.modules.evidence.shared.access.EvidenceAccess;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.persistence.AttachmentRepository;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.persistence.EvidenceRepository;
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
public class DeleteEvidenceHandler {
    private final EvidenceAccess evidenceAccess;
    private final EvidenceRepository evidenceRepository;
    private final AttachmentRepository attachmentRepository;
    private final FileStorage fileStorage;

    @Transactional
    public Result<Void> handle(UserContext user, Long evidenceId) {
        var evidence = evidenceAccess.requireEvidence(user, evidenceId, Permission.EVIDENCE_DELETE);
        if (!evidence.isSuccess()) return Result.error(evidence.getErrorType(), evidence.getErrorMessage());
        try {
            for (var attachment : attachmentRepository.findByEvidenceIdOrderByCreatedAtAsc(evidenceId)) {
                fileStorage.delete(attachment.getStorageKey());
            }
        } catch (IOException exception) {
            return Result.error("Evidence attachments could not be removed from storage");
        }
        evidenceRepository.delete(evidence.getData());
        return Result.success();
    }
}
