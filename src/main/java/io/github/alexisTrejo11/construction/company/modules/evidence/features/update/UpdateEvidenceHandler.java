package io.github.alexisTrejo11.construction.company.modules.evidence.features.update;

import io.github.alexisTrejo11.construction.company.modules.evidence.features.evidence.EvidenceCommand;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.access.EvidenceAccess;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.dto.EvidenceResponse;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.mapper.EvidenceResponseMapper;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.persistence.EvidenceRepository;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateEvidenceHandler {
    private final EvidenceAccess evidenceAccess;
    private final EvidenceRepository evidenceRepository;
    private final EvidenceResponseMapper mapper;

    @Transactional
    public Result<EvidenceResponse> handle(UserContext user, Long evidenceId, EvidenceCommand command) {
        var evidence = evidenceAccess.requireEvidence(user, evidenceId, Permission.EVIDENCE_UPDATE);
        if (!evidence.isSuccess()) return Result.error(evidence.getErrorType(), evidence.getErrorMessage());
        evidence.getData().setTitle(command.title());
        evidence.getData().setDescription(command.description());
        evidence.getData().setRecordedAt(command.recordedAt());
        return Result.success(mapper.toResponse(evidenceRepository.save(evidence.getData())));
    }
}
