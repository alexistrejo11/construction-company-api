package io.github.alexisTrejo11.construction.company.modules.expense.shared.domain;

import io.github.alexisTrejo11.construction.company.modules.budget.shared.domain.Budget;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.domain.BudgetItem;
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
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "expenses")
@Getter
@Setter
public class Expense extends AbstractJpaEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id")
    private Budget budget;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_item_id")
    private BudgetItem budgetItem;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ExpenseStatus status = ExpenseStatus.DRAFT;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @Column(length = 500)
    private String description;

    public io.github.alexisTrejo11.construction.company.shared.Result<Void> submit() {
        if (status != ExpenseStatus.DRAFT) return io.github.alexisTrejo11.construction.company.shared.Result.business("Only draft expenses can be submitted");
        status = ExpenseStatus.PENDING_APPROVAL;
        return io.github.alexisTrejo11.construction.company.shared.Result.success();
    }

    public io.github.alexisTrejo11.construction.company.shared.Result<Void> approve() {
        if (status != ExpenseStatus.PENDING_APPROVAL) return io.github.alexisTrejo11.construction.company.shared.Result.business("Only pending expenses can be approved");
        status = ExpenseStatus.APPROVED;
        return io.github.alexisTrejo11.construction.company.shared.Result.success();
    }

    public io.github.alexisTrejo11.construction.company.shared.Result<Void> reject() {
        if (status != ExpenseStatus.PENDING_APPROVAL) return io.github.alexisTrejo11.construction.company.shared.Result.business("Only pending expenses can be rejected");
        status = ExpenseStatus.REJECTED;
        return io.github.alexisTrejo11.construction.company.shared.Result.success();
    }

    public io.github.alexisTrejo11.construction.company.shared.Result<Void> returnToDraft() {
        if (status != ExpenseStatus.REJECTED) return io.github.alexisTrejo11.construction.company.shared.Result.business("Only rejected expenses can return to draft");
        status = ExpenseStatus.DRAFT;
        return io.github.alexisTrejo11.construction.company.shared.Result.success();
    }
}
