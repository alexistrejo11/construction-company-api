package io.github.alexisTrejo11.construction.company.modules.project.members.shared.persistence;

import io.github.alexisTrejo11.construction.company.modules.project.members.shared.domain.ProjectMember;
import io.github.alexisTrejo11.construction.company.modules.project.members.shared.domain.ProjectMemberStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
  Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);

  boolean existsByProjectIdAndUserIdAndStatus(
      Long projectId,
      Long userId,
      ProjectMemberStatus status
  );
}
