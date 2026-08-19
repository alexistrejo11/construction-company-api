package io.github.alexisTrejo11.construction.company.modules.project.phases.shared.domain;

import io.github.alexisTrejo11.construction.company.modules.project.shared.domain.Project;
import io.github.alexisTrejo11.construction.company.shared.persistence.AbstractJpaEntity;
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
}
