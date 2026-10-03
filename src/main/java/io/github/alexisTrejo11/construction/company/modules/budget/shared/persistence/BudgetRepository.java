package io.github.alexisTrejo11.construction.company.modules.budget.shared.persistence;

import io.github.alexisTrejo11.construction.company.modules.budget.shared.domain.Budget;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByProjectId(Long projectId);
}
