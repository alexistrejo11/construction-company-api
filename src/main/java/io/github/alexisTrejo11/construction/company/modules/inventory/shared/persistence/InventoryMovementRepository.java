package io.github.alexisTrejo11.construction.company.modules.inventory.shared.persistence;

import io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain.InventoryMovement;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain.MovementStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
    List<InventoryMovement> findByStatus(MovementStatus status);

    boolean existsByReversalOf(InventoryMovement movement);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select movement from InventoryMovement movement where movement.id = :id")
    Optional<InventoryMovement> findByIdForUpdate(@Param("id") Long id);
}
