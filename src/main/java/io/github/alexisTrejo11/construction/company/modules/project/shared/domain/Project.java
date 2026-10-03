package io.github.alexisTrejo11.construction.company.modules.project.shared.domain;

import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.persistence.AbstractJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
public class Project extends AbstractJpaEntity {
  @Column(nullable = false, length = 150)
  private String name;
  @Column(nullable = false, unique = true, length = 30)
  private String code;
  @Column(columnDefinition = "TEXT")
  private String description;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ProjectStatus status = ProjectStatus.PLANNING;
  @Column(name = "estimated_amount", nullable = false, precision = 15, scale = 2)
  private BigDecimal totalBudget = BigDecimal.ZERO;
  @Column(nullable = false, length = 3)
  private String currency = "MXN";
  @Embedded
  private SiteLocation location;
  @Column(name = "start_date")
  private LocalDate startDate;
  @Column(name = "estimated_end_date")
  private LocalDate estimatedEndDate;
  @Column(name = "actual_end_date")
  private LocalDate actualEndDate;
  public Result<Void> validate() {
    if (name == null || name.isBlank()) return Result.validation("Project name is required");
    if (code == null || code.isBlank()) return Result.validation("Project code is required");
    if (totalBudget == null || totalBudget.signum() < 0) return Result.business("Estimated amount cannot be negative");
    if (startDate != null && estimatedEndDate != null && estimatedEndDate.isBefore(startDate)) return Result.business("Estimated end date cannot precede start date");
    if (startDate != null && actualEndDate != null && actualEndDate.isBefore(startDate)) return Result.business("Actual end date cannot precede start date");
    return Result.success();
  }

  public Result<Void> updateStatus(ProjectStatus newStatus) {
    if (newStatus == null) {
      return Result.validation("Project status is required");
    }

    boolean allowed = switch (status) {
      case PLANNING -> newStatus == ProjectStatus.IN_PROGRESS || newStatus == ProjectStatus.CANCELLED;
      case IN_PROGRESS -> newStatus == ProjectStatus.ON_HOLD
          || newStatus == ProjectStatus.COMPLETED
          || newStatus == ProjectStatus.CANCELLED;
      case ON_HOLD -> newStatus == ProjectStatus.IN_PROGRESS || newStatus == ProjectStatus.CANCELLED;
      case COMPLETED, CANCELLED -> false;
    };

    if (!allowed) {
      return Result.business("Project status transition is not allowed");
    }

    this.status = newStatus;
    if (newStatus == ProjectStatus.COMPLETED && actualEndDate == null) {
      actualEndDate = LocalDate.now();
    }
    return Result.success();
  }

  public Result<Void> restore() {
    if (status != ProjectStatus.CANCELLED) {
      return Result.business("Only cancelled projects can be restored");
    }

    status = ProjectStatus.PLANNING;
    return Result.success();
  }
}
