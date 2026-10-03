package io.github.alexisTrejo11.construction.company.modules.evidence.features.get;

import io.github.alexisTrejo11.construction.company.modules.evidence.shared.access.EvidenceAccess;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.dto.EvidenceResponse;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.mapper.EvidenceResponseMapper;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetEvidenceHandler {
    private final EvidenceAccess evidenceAccess;
    private final EvidenceResponseMapper mapper;

    @Transactional(readOnly = true)
    public Result<EvidenceResponse> handle(UserContext user, Long evidenceId) {
        var evidence = evidenceAccess.requireEvidence(user, evidenceId, Permission.EVIDENCE_READ);
        if (!evidence.isSuccess()) return Result.error(evidence.getErrorType(), evidence.getErrorMessage());
        return Result.success(mapper.toResponse(evidence.getData()));
    }
}
