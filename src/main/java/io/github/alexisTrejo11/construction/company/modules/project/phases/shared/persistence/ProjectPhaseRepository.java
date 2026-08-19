package io.github.alexisTrejo11.construction.company.modules.project.phases.shared.persistence;

import io.github.alexisTrejo11.construction.company.modules.project.phases.shared.domain.ProjectPhase;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectPhaseRepository extends JpaRepository<ProjectPhase, Long> {
  List<ProjectPhase> findByProjectIdOrderBySequenceOrder(Long projectId);
}
