package io.github.alexisTrejo11.construction.company.modules.budget.shared.persistence;

import io.github.alexisTrejo11.construction.company.modules.budget.shared.domain.BudgetItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetItemRepository extends JpaRepository<BudgetItem, Long> {
    List<BudgetItem> findByBudgetIdOrderByCreatedAtAsc(Long budgetId);
    boolean existsByBudgetIdAndDescription(Long budgetId, String description);
}
