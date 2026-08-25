package io.github.alexisTrejo11.construction.company.modules.project.phases.shared.persistence;

import io.github.alexisTrejo11.construction.company.modules.project.phases.shared.domain.ProjectPhase;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProjectPhaseRepository extends JpaRepository<ProjectPhase, Long> {
  List<ProjectPhase> findByProjectIdOrderBySequenceOrder(Long projectId);

  Optional<ProjectPhase> findByIdAndProjectId(Long phaseId, Long projectId);

    boolean existsByProjectIdAndSequenceOrder(Long projectId, Integer sequenceOrder);

    boolean existsByProjectIdAndSequenceOrderAndIdNot(
        Long projectId,
        Integer sequenceOrder,
        Long phaseId
    );
}
