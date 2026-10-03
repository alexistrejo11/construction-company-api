package io.github.alexisTrejo11.construction.company.modules.evidence.features.listattachments;

import io.github.alexisTrejo11.construction.company.modules.evidence.shared.access.EvidenceAccess;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.dto.AttachmentResponse;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.mapper.EvidenceResponseMapper;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.persistence.AttachmentRepository;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListAttachmentsHandler {
    private final EvidenceAccess evidenceAccess;
    private final AttachmentRepository attachmentRepository;
    private final EvidenceResponseMapper mapper;

    @Transactional(readOnly = true)
    public Result<List<AttachmentResponse>> handle(UserContext user, Long evidenceId) {
        var evidence = evidenceAccess.requireEvidence(user, evidenceId, Permission.ATTACHMENT_READ);
        if (!evidence.isSuccess()) return Result.error(evidence.getErrorType(), evidence.getErrorMessage());
        return Result.success(attachmentRepository.findByEvidenceIdOrderByCreatedAtAsc(evidenceId).stream().map(mapper::toResponse).toList());
    }
}
