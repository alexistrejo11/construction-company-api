package io.github.alexisTrejo11.construction.company.modules.project.phases.shared.domain;

import io.github.alexisTrejo11.construction.company.modules.project.shared.domain.Project;
import io.github.alexisTrejo11.construction.company.shared.persistence.AbstractJpaEntity;
import io.github.alexisTrejo11.construction.company.shared.Result;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "project_phases")
@Getter
@Setter
public class ProjectPhase extends AbstractJpaEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;
  @Column(nullable = false, length = 100)
  private String name;
  @Column(name = "sequence_order", nullable = false)
  private Integer sequenceOrder;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ProjectPhaseStatus status = ProjectPhaseStatus.PLANNED;
  @Column(name = "allocated_amount", nullable = false, precision = 15, scale = 2)
  private BigDecimal allocatedAmount = BigDecimal.ZERO;
  @Column(name = "start_date")
  private LocalDate startDate;
  @Column(name = "end_date")
  private LocalDate endDate;

  public Result<Void> validate() {
    if (name == null || name.isBlank()) {
      return Result.validation("Phase name is required");
    }
    if (sequenceOrder == null || sequenceOrder < 1) {
      return Result.validation("Sequence order must be at least 1");
    }
    if (allocatedAmount == null || allocatedAmount.signum() < 0) {
      return Result.validation("Allocated budget must be zero or positive");
    }
    if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
      return Result.business("Phase end date cannot precede start date");
    }
    return Result.success();
  }

  public Result<Void> updateStatus(ProjectPhaseStatus newStatus) {
    if (newStatus == null) {
      return Result.validation("Phase status is required");
    }

    boolean allowed = switch (status) {
      case PLANNED -> newStatus == ProjectPhaseStatus.IN_PROGRESS
          || newStatus == ProjectPhaseStatus.CANCELLED;
      case IN_PROGRESS -> newStatus == ProjectPhaseStatus.ON_HOLD
          || newStatus == ProjectPhaseStatus.COMPLETED
          || newStatus == ProjectPhaseStatus.CANCELLED;
      case ON_HOLD -> newStatus == ProjectPhaseStatus.IN_PROGRESS
          || newStatus == ProjectPhaseStatus.CANCELLED;
      case COMPLETED, CANCELLED -> false;
    };

    if (!allowed) {
      return Result.business("Phase status transition is not allowed");
    }

    status = newStatus;
    return Result.success();
  }
}
