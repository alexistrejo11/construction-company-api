package io.github.alexisTrejo11.construction.company.modules.evidence.features.listbyexpense;

import io.github.alexisTrejo11.construction.company.modules.evidence.shared.access.EvidenceAccess;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.dto.EvidenceResponse;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.mapper.EvidenceResponseMapper;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.persistence.EvidenceRepository;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListExpenseEvidenceHandler {
    private final EvidenceAccess evidenceAccess;
    private final EvidenceRepository evidenceRepository;
    private final EvidenceResponseMapper mapper;

    @Transactional(readOnly = true)
    public Result<List<EvidenceResponse>> handle(UserContext user, Long expenseId) {
        var expense = evidenceAccess.requireExpense(user, expenseId, Permission.EVIDENCE_READ);
        if (!expense.isSuccess()) return Result.error(expense.getErrorType(), expense.getErrorMessage());
        return Result.success(evidenceRepository.findByExpenseIdOrderByRecordedAtDesc(expenseId).stream().map(mapper::toResponse).toList());
    }
}
