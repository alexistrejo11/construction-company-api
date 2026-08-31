package io.github.alexisTrejo11.construction.company.modules.budget.shared.domain;

import io.github.alexisTrejo11.construction.company.modules.project.shared.domain.Project;
import io.github.alexisTrejo11.construction.company.shared.Result;
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
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "budgets")
@Getter
@Setter
public class Budget extends AbstractJpaEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false, unique = true)
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BudgetStatus status = BudgetStatus.DRAFT;

    @Column(nullable = false, length = 3)
    private String currency;

    public Result<Void> approve() {
        if (status != BudgetStatus.DRAFT) return Result.business("Only draft budgets can be approved");
        status = BudgetStatus.APPROVED;
        return Result.success();
    }

    public Result<Void> revise() {
        if (status != BudgetStatus.APPROVED) return Result.business("Only approved budgets can be revised");
        status = BudgetStatus.DRAFT;
        return Result.success();
    }

    public Result<Void> close() {
        if (status != BudgetStatus.APPROVED) return Result.business("Only approved budgets can be closed");
        status = BudgetStatus.CLOSED;
        return Result.success();
    }
}
