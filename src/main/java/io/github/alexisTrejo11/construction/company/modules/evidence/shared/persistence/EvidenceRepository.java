package io.github.alexisTrejo11.construction.company.modules.evidence.shared.persistence;

import io.github.alexisTrejo11.construction.company.modules.evidence.shared.domain.Evidence;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvidenceRepository extends JpaRepository<Evidence, Long> {
    List<Evidence> findByPhaseIdOrderByRecordedAtDesc(Long phaseId);

    List<Evidence> findByExpenseIdOrderByRecordedAtDesc(Long expenseId);
}
