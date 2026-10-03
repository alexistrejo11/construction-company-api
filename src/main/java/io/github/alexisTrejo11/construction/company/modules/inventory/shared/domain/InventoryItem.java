package io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain;

import io.github.alexisTrejo11.construction.company.shared.persistence.AbstractJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventory_items")
@Getter
@Setter
@NoArgsConstructor
public class InventoryItem extends AbstractJpaEntity {
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InventoryCategory category;

    @Column(nullable = false, length = 30)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "tracking_mode", nullable = false, length = 20)
    private TrackingMode trackingMode;

    @Column(nullable = false)
    private boolean active = true;
}
