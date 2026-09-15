package io.github.alexisTrejo11.construction.company.modules.inventory.shared.persistence;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain.InventoryLocation;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryLocationRepository extends JpaRepository<InventoryLocation, Long> {
    Optional<InventoryLocation> findByCode(String code);

    boolean existsByCode(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select location from InventoryLocation location where location.id = :id")
    Optional<InventoryLocation> findByIdForUpdate(@Param("id") Long id);
}
