package io.github.alexisTrejo11.construction.company.modules.evidence.features.createforexpense;

import io.github.alexisTrejo11.construction.company.modules.evidence.features.evidence.EvidenceCommand;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.access.EvidenceAccess;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.domain.Evidence;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.dto.EvidenceResponse;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.mapper.EvidenceResponseMapper;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.persistence.EvidenceRepository;
import io.github.alexisTrejo11.construction.company.modules.user.shared.persistence.UserRepository;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateExpenseEvidenceHandler {
    private final EvidenceAccess evidenceAccess;
    private final EvidenceRepository evidenceRepository;
    private final UserRepository userRepository;
    private final EvidenceResponseMapper mapper;

    @Transactional
    public Result<EvidenceResponse> handle(UserContext user, Long expenseId, EvidenceCommand command) {
        var expense = evidenceAccess.requireExpense(user, expenseId, Permission.EVIDENCE_CREATE);
        if (!expense.isSuccess()) return Result.error(expense.getErrorType(), expense.getErrorMessage());
        var author = userRepository.findById(user.userId());
        if (author.isEmpty()) return Result.unauthenticated("Authenticated user was not found");
        var evidence = new Evidence();
        evidence.setExpense(expense.getData());
        evidence.setAuthor(author.get());
        evidence.setTitle(command.title());
        evidence.setDescription(command.description());
        evidence.setRecordedAt(command.recordedAt());
        return Result.success(mapper.toResponse(evidenceRepository.save(evidence)));
    }
}
