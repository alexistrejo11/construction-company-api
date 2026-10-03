package io.github.alexisTrejo11.construction.company.modules.inventory.shared.dto;

import io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain.AdjustmentDirection;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain.InventoryCategory;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain.LocationType;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain.MovementStatus;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain.MovementType;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain.TrackingMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public final class InventoryDtos {
    private InventoryDtos() {
    }

    public record ItemRequest(@NotBlank @Size(max = 50) String code, @NotBlank @Size(max = 200) String name, @NotNull InventoryCategory category, @NotBlank @Size(max = 30) String unit, @NotNull TrackingMode trackingMode) { }
    public record ItemStatusRequest(@NotNull Boolean active) { }
    public record ItemResponse(Long id, String code, String name, InventoryCategory category, String unit, TrackingMode trackingMode, boolean active) { }
    public record LocationRequest(@NotBlank @Size(max = 50) String code, @NotBlank @Size(max = 200) String name, @NotNull LocationType type, Long projectId, @Size(max = 500) String address) { }
    public record LocationStatusRequest(@NotNull Boolean active) { }
    public record LocationResponse(Long id, String code, String name, LocationType type, Long projectId, String address, boolean active) { }
    public record LineRequest(@NotNull Long itemId, @NotNull @DecimalMin(value = "0.0001") @Digits(integer = 12, fraction = 3) BigDecimal quantity, @Size(max = 150) String serialNumber) { }
    public record MovementRequest(@NotNull MovementType type, Long sourceLocationId, Long targetLocationId, Long locationId, AdjustmentDirection adjustmentDirection, @NotEmpty List<@Valid LineRequest> lines) { }
    public record LineResponse(Long itemId, String itemCode, BigDecimal quantity, String serialNumber) { }
    public record MovementResponse(Long id, MovementType type, MovementStatus status, Long sourceLocationId, Long targetLocationId, AdjustmentDirection adjustmentDirection, List<LineResponse> lines, Long reversalOfId) { }
    public record BalanceLine(Long locationId, String locationCode, BigDecimal quantity, List<String> serialNumbers) { }
    public record ItemBalanceResponse(ItemResponse item, List<BalanceLine> balances) { }
    public record LocationBalanceResponse(LocationResponse location, List<ItemBalance> balances) { }
    public record ItemBalance(Long itemId, String itemCode, BigDecimal quantity, List<String> serialNumbers) { }
}
