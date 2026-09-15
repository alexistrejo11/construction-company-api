package io.github.alexisTrejo11.construction.company.modules.inventory.shared.persistence;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain.InventoryItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    Optional<InventoryItem> findByCode(String code);

    boolean existsByCode(String code);
}
