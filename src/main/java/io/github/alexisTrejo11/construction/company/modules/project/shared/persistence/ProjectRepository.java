package io.github.alexisTrejo11.construction.company.modules.project.shared.persistence;

import io.github.alexisTrejo11.construction.company.modules.project.shared.domain.ProjectStatus;
import io.github.alexisTrejo11.construction.company.modules.project.shared.dto.ProjectsGlobalSummaryResponse;
import io.github.alexisTrejo11.construction.company.modules.project.shared.domain.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

  @Query("""
        SELECT p FROM Project p
        WHERE (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:status IS NULL OR p.status = :status)
          AND (:city IS NULL OR LOWER(p.location.city) LIKE LOWER(CONCAT('%', :city, '%')))
    """)
  Page<Project> findWithFilters(
      @Param("search") String search,
      @Param("status") ProjectStatus status,
      @Param("city") String city,
      Pageable pageable
  );

  @Query("""
        SELECT DISTINCT p FROM Project p
        JOIN io.github.alexisTrejo11.construction.company.modules.project.members.shared.domain.ProjectMember m ON m.project = p
        WHERE m.user.id = :userId
          AND m.status = io.github.alexisTrejo11.construction.company.modules.project.members.shared.domain.ProjectMemberStatus.ACTIVE
          AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:status IS NULL OR p.status = :status)
          AND (:city IS NULL OR LOWER(p.location.city) LIKE LOWER(CONCAT('%', :city, '%')))
    """)
  Page<Project> findVisibleByUser(
      @Param("userId") Long userId,
      @Param("search") String search,
      @Param("status") ProjectStatus status,
      @Param("city") String city,
      Pageable pageable
  );

  @Query("""
        SELECT DISTINCT p FROM Project p
         JOIN io.github.alexisTrejo11.construction.company.modules.project.members.shared.domain.ProjectMember m ON m.project = p
         WHERE m.user.id = :userId
           AND m.status = io.github.alexisTrejo11.construction.company.modules.project.members.shared.domain.ProjectMemberStatus.ACTIVE
    """)
  Page<Project> findByUserIdWithMembers(@Param("userId") Long userId, Pageable pageable);

  @Query("""
        SELECT new io.github.alexisTrejo11.construction.company.modules.project.shared.dto.ProjectsGlobalSummaryResponse(
            COUNT(p),
            COALESCE(SUM(CASE WHEN p.status = io.github.alexisTrejo11.construction.company.modules.project.shared.domain.ProjectStatus.PLANNING THEN 1L ELSE 0L END), 0L),
            COALESCE(SUM(CASE WHEN p.status = io.github.alexisTrejo11.construction.company.modules.project.shared.domain.ProjectStatus.IN_PROGRESS THEN 1L ELSE 0L END), 0L),
            COALESCE(SUM(CASE WHEN p.status = io.github.alexisTrejo11.construction.company.modules.project.shared.domain.ProjectStatus.ON_HOLD THEN 1L ELSE 0L END), 0L),
            COALESCE(SUM(CASE WHEN p.status = io.github.alexisTrejo11.construction.company.modules.project.shared.domain.ProjectStatus.COMPLETED THEN 1L ELSE 0L END), 0L),
            COALESCE(SUM(CASE WHEN p.status = io.github.alexisTrejo11.construction.company.modules.project.shared.domain.ProjectStatus.CANCELLED THEN 1L ELSE 0L END), 0L),
             COALESCE(SUM(p.totalBudget), 0)
        )
        FROM Project p
    """)
  ProjectsGlobalSummaryResponse getGlobalSummary();

  Optional<Project> findByCode(String code);

  boolean existsByCode(String code);
}
