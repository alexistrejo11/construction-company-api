package io.github.alexisTrejo11.construction.company.modules.budget.shared.domain;

import io.github.alexisTrejo11.construction.company.modules.project.phases.shared.domain.ProjectPhase;
import io.github.alexisTrejo11.construction.company.shared.persistence.AbstractJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "budget_items")
@Getter
@Setter
public class BudgetItem extends AbstractJpaEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id", nullable = false)
    private Budget budget;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phase_id")
    private ProjectPhase phase;

    @Column(nullable = false, length = 200)
    private String description;
    @Column(length = 100)
    private String category;
    @Column(length = 50)
    private String unit;
    @Column(name = "planned_quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal plannedQuantity;
    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    public BigDecimal plannedTotal() {
        return plannedQuantity.multiply(unitPrice);
    }
}
