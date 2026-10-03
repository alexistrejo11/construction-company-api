package io.github.alexisTrejo11.construction.company.modules.expense.shared.persistence;

import io.github.alexisTrejo11.construction.company.modules.expense.shared.domain.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByBudgetIdOrderByCreatedAtDesc(Long budgetId);
    List<Expense> findByBudgetItemId(Long budgetItemId);
}
