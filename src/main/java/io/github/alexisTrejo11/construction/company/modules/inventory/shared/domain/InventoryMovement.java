package io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain;

import io.github.alexisTrejo11.construction.company.shared.persistence.AbstractJpaEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventory_movements")
@Getter
@Setter
@NoArgsConstructor
public class InventoryMovement extends AbstractJpaEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MovementType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MovementStatus status = MovementStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_location_id")
    private InventoryLocation sourceLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_location_id")
    private InventoryLocation targetLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "adjustment_direction", length = 20)
    private AdjustmentDirection adjustmentDirection;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reversal_of_id", unique = true)
    private InventoryMovement reversalOf;

    @OneToMany(mappedBy = "movement", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<InventoryMovementLine> lines = new ArrayList<>();

    public void replaceLines(List<InventoryMovementLine> replacement) {
        lines.clear();
        replacement.forEach(line -> {
            line.setMovement(this);
            lines.add(line);
        });
    }
}
