package io.github.alexisTrejo11.construction.company.modules.inventory.features;

import io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain.AdjustmentDirection;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain.InventoryCategory;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain.InventoryItem;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain.InventoryLocation;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain.InventoryMovement;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain.InventoryMovementLine;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain.LocationType;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain.MovementStatus;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain.MovementType;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain.TrackingMode;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.dto.InventoryDtos.BalanceLine;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.dto.InventoryDtos.ItemBalance;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.dto.InventoryDtos.ItemBalanceResponse;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.dto.InventoryDtos.ItemRequest;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.dto.InventoryDtos.ItemResponse;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.dto.InventoryDtos.ItemStatusRequest;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.dto.InventoryDtos.LineRequest;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.dto.InventoryDtos.LineResponse;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.dto.InventoryDtos.LocationBalanceResponse;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.dto.InventoryDtos.LocationRequest;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.dto.InventoryDtos.LocationResponse;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.dto.InventoryDtos.LocationStatusRequest;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.dto.InventoryDtos.MovementRequest;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.dto.InventoryDtos.MovementResponse;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.persistence.InventoryItemRepository;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.persistence.InventoryLocationRepository;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.persistence.InventoryMovementRepository;
import io.github.alexisTrejo11.construction.company.modules.project.shared.persistence.ProjectRepository;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.GlobalAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.PageResponse;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryHandler {
    private final InventoryItemRepository items;
    private final InventoryLocationRepository locations;
    private final InventoryMovementRepository movements;
    private final ProjectRepository projects;
    private final GlobalAuthorizationPolicy authorization;

    private <T> Result<T> permission(UserContext user, Permission required) {
        Result<Void> result = authorization.requirePermission(user, required);
        if (!result.isSuccess()) {
            return Result.error(result.getErrorType(), result.getErrorMessage());
        }
        return null;
    }

    @Transactional
    public Result<ItemResponse> createItem(UserContext user, ItemRequest request) {
        Result<ItemResponse> denied = permission(user, Permission.INVENTORY_ITEM_CREATE);
        if (denied != null) return denied;
        if (items.existsByCode(request.code())) return Result.conflict("Item code already exists");
        InventoryItem item = new InventoryItem();
        applyItem(item, request);
        return Result.success(toItemResponse(items.save(item)));
    }

    @Transactional(readOnly = true)
    public Result<PageResponse<ItemResponse>> listItems(UserContext user, String search,
            InventoryCategory category, TrackingMode trackingMode, Boolean active,
            int page, int size, String sort) {
        Result<PageResponse<ItemResponse>> denied = permission(user, Permission.INVENTORY_ITEM_READ);
        if (denied != null) return denied;
        if (!validPage(page, size)) return Result.validation("Invalid pagination parameters");
        String query = search == null ? null : search.toLowerCase();
        List<ItemResponse> result = items.findAll().stream()
                .filter(item -> query == null || item.getName().toLowerCase().contains(query)
                        || item.getCode().toLowerCase().contains(query))
                .filter(item -> category == null || item.getCategory() == category)
                .filter(item -> trackingMode == null || item.getTrackingMode() == trackingMode)
                .filter(item -> active == null || item.isActive() == active)
                .map(this::toItemResponse)
                .sorted(itemComparator(sort))
                .toList();
        return Result.success(page(result, page, size));
    }

    @Transactional(readOnly = true)
    public Result<ItemResponse> getItem(UserContext user, Long id) {
        Result<ItemResponse> denied = permission(user, Permission.INVENTORY_ITEM_READ);
        if (denied != null) return denied;
        return items.findById(id).map(item -> Result.success(toItemResponse(item)))
                .orElseGet(() -> Result.notFound("Inventory item was not found"));
    }

    @Transactional
    public Result<ItemResponse> updateItem(UserContext user, Long id, ItemRequest request) {
        Result<ItemResponse> denied = permission(user, Permission.INVENTORY_ITEM_UPDATE);
        if (denied != null) return denied;
        Optional<InventoryItem> found = items.findById(id);
        if (found.isEmpty()) return Result.notFound("Inventory item was not found");
        InventoryItem item = found.get();
        if (!item.getCode().equals(request.code()) && items.existsByCode(request.code())) {
            return Result.conflict("Item code already exists");
        }
        if (item.getTrackingMode() != request.trackingMode() && hasHistory(item)) {
            return Result.conflict("Tracking mode cannot change after movement history exists");
        }
        applyItem(item, request);
        return Result.success(toItemResponse(item));
    }

    @Transactional
    public Result<ItemResponse> itemStatus(UserContext user, Long id, ItemStatusRequest request) {
        Result<ItemResponse> denied = permission(user, Permission.INVENTORY_ITEM_STATUS_MANAGE);
        if (denied != null) return denied;
        return items.findById(id).map(item -> {
            item.setActive(request.active());
            return Result.success(toItemResponse(item));
        }).orElseGet(() -> Result.notFound("Inventory item was not found"));
    }

    @Transactional
    public Result<LocationResponse> createLocation(UserContext user, LocationRequest request) {
        Result<LocationResponse> denied = permission(user, Permission.INVENTORY_LOCATION_CREATE);
        if (denied != null) return denied;
        if (locations.existsByCode(request.code())) return Result.conflict("Location code already exists");
        Result<InventoryLocation> location = makeLocation(request);
        if (!location.isSuccess()) return Result.error(location.getErrorType(), location.getErrorMessage());
        return Result.success(toLocationResponse(locations.save(location.getData())));
    }

    @Transactional(readOnly = true)
    public Result<PageResponse<LocationResponse>> listLocations(UserContext user, String search,
            LocationType type, Long projectId, Boolean active, int page, int size, String sort) {
        Result<PageResponse<LocationResponse>> denied = permission(user, Permission.INVENTORY_LOCATION_READ);
        if (denied != null) return denied;
        if (!validPage(page, size)) return Result.validation("Invalid pagination parameters");
        String query = search == null ? null : search.toLowerCase();
        List<LocationResponse> result = locations.findAll().stream()
                .filter(location -> query == null || location.getName().toLowerCase().contains(query)
                        || location.getCode().toLowerCase().contains(query))
                .filter(location -> type == null || location.getType() == type)
                .filter(location -> projectId == null || location.getProject() != null
                        && projectId.equals(location.getProject().getId()))
                .filter(location -> active == null || location.isActive() == active)
                .map(this::toLocationResponse)
                .sorted(locationComparator(sort))
                .toList();
        return Result.success(page(result, page, size));
    }

    @Transactional(readOnly = true)
    public Result<LocationResponse> getLocation(UserContext user, Long id) {
        Result<LocationResponse> denied = permission(user, Permission.INVENTORY_LOCATION_READ);
        if (denied != null) return denied;
        return locations.findById(id).map(location -> Result.success(toLocationResponse(location)))
                .orElseGet(() -> Result.notFound("Inventory location was not found"));
    }

    @Transactional
    public Result<LocationResponse> updateLocation(UserContext user, Long id, LocationRequest request) {
        Result<LocationResponse> denied = permission(user, Permission.INVENTORY_LOCATION_UPDATE);
        if (denied != null) return denied;
        Optional<InventoryLocation> found = locations.findById(id);
        if (found.isEmpty()) return Result.notFound("Inventory location was not found");
        InventoryLocation location = found.get();
        if (!location.getCode().equals(request.code()) && locations.existsByCode(request.code())) {
            return Result.conflict("Location code already exists");
        }
        Result<InventoryLocation> replacement = makeLocation(request);
        if (!replacement.isSuccess()) return Result.error(replacement.getErrorType(), replacement.getErrorMessage());
        location.setCode(request.code());
        location.setName(request.name());
        location.setType(request.type());
        location.setProject(replacement.getData().getProject());
        location.setAddress(request.address());
        return Result.success(toLocationResponse(location));
    }

    @Transactional
    public Result<LocationResponse> locationStatus(UserContext user, Long id, LocationStatusRequest request) {
        Result<LocationResponse> denied = permission(user, Permission.INVENTORY_LOCATION_STATUS_MANAGE);
        if (denied != null) return denied;
        return locations.findById(id).map(location -> {
            location.setActive(request.active());
            return Result.success(toLocationResponse(location));
        }).orElseGet(() -> Result.notFound("Inventory location was not found"));
    }

    @Transactional
    public Result<MovementResponse> createMovement(UserContext user, MovementRequest request) {
        Result<MovementResponse> denied = permission(user, Permission.INVENTORY_MOVEMENT_CREATE);
        if (denied != null) return denied;
        InventoryMovement movement = new InventoryMovement();
        Result<Void> valid = fill(movement, request);
        if (!valid.isSuccess()) return Result.error(valid.getErrorType(), valid.getErrorMessage());
        return Result.success(toMovementResponse(movements.save(movement)));
    }

    @Transactional
    public Result<MovementResponse> updateMovement(UserContext user, Long id, MovementRequest request) {
        Result<MovementResponse> denied = permission(user, Permission.INVENTORY_MOVEMENT_UPDATE);
        if (denied != null) return denied;
        Optional<InventoryMovement> found = movements.findById(id);
        if (found.isEmpty()) return Result.notFound("Inventory movement was not found");
        InventoryMovement movement = found.get();
        if (movement.getStatus() != MovementStatus.DRAFT) return Result.conflict("Posted movements are immutable");
        Result<Void> valid = fill(movement, request);
        if (!valid.isSuccess()) return Result.error(valid.getErrorType(), valid.getErrorMessage());
        return Result.success(toMovementResponse(movement));
    }

    private Result<Void> fill(InventoryMovement movement, MovementRequest request) {
        if (request.lines() == null || request.lines().isEmpty()) {
            return Result.validation("Movement must contain at least one line");
        }
        if (!validDirection(request)) return Result.validation("Movement direction is invalid");
        if (request.type() == MovementType.TRANSFER
                && Objects.equals(request.sourceLocationId(), request.targetLocationId())) {
            return Result.validation("Transfer locations must differ");
        }
        if (!validLocations(request)) return Result.notFound("Movement location was not found");
        InventoryLocation source = findLocation(sourceLocationId(request));
        InventoryLocation target = findLocation(request.targetLocationId());
        if (source != null && !source.isActive() || target != null && !target.isActive()) {
            return Result.business("Inactive locations cannot be used");
        }

        Set<String> serialsInMovement = new HashSet<>();
        List<InventoryMovementLine> lines = new ArrayList<>();
        for (LineRequest requestLine : request.lines()) {
            Optional<InventoryItem> foundItem = items.findById(requestLine.itemId());
            if (foundItem.isEmpty()) return Result.notFound("Inventory item was not found");
            InventoryItem item = foundItem.get();
            if (!item.isActive()) return Result.business("Inactive items cannot be used");
            Result<Void> lineValid = validateLine(item, requestLine, serialsInMovement);
            if (!lineValid.isSuccess()) return lineValid;
            InventoryMovementLine line = new InventoryMovementLine();
            line.setItem(item);
            line.setQuantity(requestLine.quantity());
            line.setSerialNumber(normalizedSerial(requestLine.serialNumber()));
            lines.add(line);
        }
        movement.setType(request.type());
        movement.setSourceLocation(source);
        movement.setTargetLocation(target);
        movement.setAdjustmentDirection(request.adjustmentDirection());
        movement.replaceLines(lines);
        return Result.success();
    }

    private Result<Void> validateLine(InventoryItem item, LineRequest line, Set<String> serials) {
        if (item.getTrackingMode() == TrackingMode.SERIALIZED) {
            if (line.quantity().compareTo(BigDecimal.ONE) != 0
                    || line.serialNumber() == null || line.serialNumber().isBlank()) {
                return Result.validation("Serialized lines require quantity 1 and a serial number");
            }
            if (!serials.add(line.serialNumber().trim())) return Result.conflict("Serial number is duplicated in the movement");
        } else if (line.serialNumber() != null && !line.serialNumber().isBlank()) {
            return Result.validation("Quantity-tracked lines cannot have a serial number");
        }
        return Result.success();
    }

    @Transactional
    public Result<MovementResponse> post(UserContext user, Long id) {
        Result<MovementResponse> denied = permission(user, Permission.INVENTORY_MOVEMENT_POST);
        if (denied != null) return denied;
        Optional<InventoryMovement> found = movements.findByIdForUpdate(id);
        if (found.isEmpty()) return Result.notFound("Inventory movement was not found");
        InventoryMovement movement = found.get();
        if (movement.getStatus() != MovementStatus.DRAFT) return Result.conflict("Movement is already posted");
        lockLocations(movement);
        Result<Void> valid = validateBalance(movement);
        if (!valid.isSuccess()) return Result.error(valid.getErrorType(), valid.getErrorMessage());
        movement.setStatus(MovementStatus.POSTED);
        return Result.success(toMovementResponse(movement));
    }

    private void lockLocations(InventoryMovement movement) {
        List<Long> ids = new ArrayList<>();
        if (movement.getSourceLocation() != null) ids.add(movement.getSourceLocation().getId());
        if (movement.getTargetLocation() != null) ids.add(movement.getTargetLocation().getId());
        ids.stream().distinct().sorted().forEach(locations::findByIdForUpdate);
    }

    private Result<Void> validateBalance(InventoryMovement movement) {
        Map<String, BigDecimal> balances = balances();
        Map<String, BigDecimal> requested = new HashMap<>();
        for (InventoryMovementLine line : movement.getLines()) {
            InventoryItem item = line.getItem();
            if (!item.isActive()) return Result.business("Inactive items cannot be used");
            if (reduces(movement)) {
                if (item.getTrackingMode() == TrackingMode.QUANTITY) {
                    String balanceKey = key(item, movement.getSourceLocation());
                    BigDecimal totalRequested = requested.merge(balanceKey, line.getQuantity(), BigDecimal::add);
                    if (balances.getOrDefault(balanceKey, BigDecimal.ZERO).compareTo(totalRequested) < 0) {
                        return Result.business("Insufficient stock");
                    }
                }
                if (item.getTrackingMode() == TrackingMode.SERIALIZED
                        && !serials(item, movement.getSourceLocation()).contains(line.getSerialNumber())) {
                    return Result.business("Serialized item is not available at source");
                }
            } else if (item.getTrackingMode() == TrackingMode.SERIALIZED
                    && serialExistsAtAnyLocation(item, line.getSerialNumber())) {
                return Result.conflict("Serial number is already in inventory");
            }
        }
        return Result.success();
    }

    private boolean serialExistsAtAnyLocation(InventoryItem item, String serial) {
        return locations.findAll().stream().anyMatch(location -> serials(item, location).contains(serial));
    }

    private boolean reduces(InventoryMovement movement) {
        return movement.getType() == MovementType.ISSUE || movement.getType() == MovementType.RETURN
                || movement.getType() == MovementType.TRANSFER
                || movement.getType() == MovementType.ADJUSTMENT
                        && movement.getAdjustmentDirection() == AdjustmentDirection.DECREASE;
    }

    private Map<String, BigDecimal> balances() {
        Map<String, BigDecimal> result = new HashMap<>();
        for (InventoryMovement movement : movements.findByStatus(MovementStatus.POSTED)) {
            for (InventoryMovementLine line : movement.getLines()) apply(result, movement, line);
        }
        return result;
    }

    private void apply(Map<String, BigDecimal> balances, InventoryMovement movement, InventoryMovementLine line) {
        if (line.getItem().getTrackingMode() == TrackingMode.SERIALIZED) return;
        BigDecimal amount = line.getQuantity();
        if (movement.getType() == MovementType.ADJUSTMENT) {
            if (movement.getAdjustmentDirection() == AdjustmentDirection.DECREASE) amount = amount.negate();
            balances.merge(key(line.getItem(), movement.getSourceLocation()), amount, BigDecimal::add);
            return;
        }
        if (movement.getSourceLocation() != null) balances.merge(key(line.getItem(), movement.getSourceLocation()), amount.negate(), BigDecimal::add);
        if (movement.getTargetLocation() != null) balances.merge(key(line.getItem(), movement.getTargetLocation()), amount, BigDecimal::add);
    }

    private Set<String> serials(InventoryItem item, InventoryLocation location) {
        Set<String> result = new HashSet<>();
        if (location == null) return result;
        for (InventoryMovement movement : movements.findByStatus(MovementStatus.POSTED)) {
            for (InventoryMovementLine line : movement.getLines()) {
                if (!item.getId().equals(line.getItem().getId()) || line.getSerialNumber() == null) continue;
                if (movement.getType() == MovementType.ADJUSTMENT) {
                    if (movement.getSourceLocation().getId().equals(location.getId())) {
                        if (movement.getAdjustmentDirection() == AdjustmentDirection.INCREASE) result.add(line.getSerialNumber());
                        else result.remove(line.getSerialNumber());
                    }
                } else {
                    if (movement.getSourceLocation() != null && movement.getSourceLocation().getId().equals(location.getId())) result.remove(line.getSerialNumber());
                    if (movement.getTargetLocation() != null && movement.getTargetLocation().getId().equals(location.getId())) result.add(line.getSerialNumber());
                }
            }
        }
        return result;
    }

    @Transactional
    public Result<MovementResponse> reverse(UserContext user, Long id) {
        Result<MovementResponse> denied = permission(user, Permission.INVENTORY_MOVEMENT_REVERSE);
        if (denied != null) return denied;
        Optional<InventoryMovement> found = movements.findById(id);
        if (found.isEmpty()) return Result.notFound("Inventory movement was not found");
        InventoryMovement original = found.get();
        if (original.getStatus() != MovementStatus.POSTED) return Result.conflict("Only posted movements can be reversed");
        if (original.getReversalOf() != null || movements.existsByReversalOf(original)) return Result.conflict("Movement can only be reversed once");
        InventoryMovement reversal = new InventoryMovement();
        reversal.setType(inverseType(original.getType()));
        if (original.getType() == MovementType.ADJUSTMENT) {
            reversal.setSourceLocation(original.getSourceLocation());
            reversal.setTargetLocation(null);
        } else {
            reversal.setSourceLocation(original.getTargetLocation());
            reversal.setTargetLocation(original.getSourceLocation());
        }
        reversal.setAdjustmentDirection(inverseDirection(original.getAdjustmentDirection()));
        reversal.setReversalOf(original);
        List<InventoryMovementLine> lines = original.getLines().stream().map(this::copyLine).toList();
        reversal.replaceLines(lines);
        return Result.success(toMovementResponse(movements.save(reversal)));
    }

    private InventoryMovementLine copyLine(InventoryMovementLine original) {
        InventoryMovementLine line = new InventoryMovementLine();
        line.setItem(original.getItem());
        line.setQuantity(original.getQuantity());
        line.setSerialNumber(original.getSerialNumber());
        return line;
    }

    private MovementType inverseType(MovementType type) {
        return switch (type) {
            case RECEIPT -> MovementType.ISSUE;
            case ISSUE, RETURN -> MovementType.RECEIPT;
            case TRANSFER, ADJUSTMENT -> type;
        };
    }

    private AdjustmentDirection inverseDirection(AdjustmentDirection direction) {
        if (direction == AdjustmentDirection.INCREASE) return AdjustmentDirection.DECREASE;
        if (direction == AdjustmentDirection.DECREASE) return AdjustmentDirection.INCREASE;
        return null;
    }

    @Transactional(readOnly = true)
    public Result<PageResponse<MovementResponse>> listMovements(UserContext user, MovementType type,
            MovementStatus status, Long sourceId, Long targetId, Long itemId, LocalDate from,
            LocalDate to, int page, int size, String sort) {
        Result<PageResponse<MovementResponse>> denied = permission(user, Permission.INVENTORY_MOVEMENT_READ);
        if (denied != null) return denied;
        if (!validPage(page, size)) return Result.validation("Invalid pagination parameters");
        List<MovementResponse> result = movements.findAll().stream()
                .filter(movement -> type == null || movement.getType() == type)
                .filter(movement -> status == null || movement.getStatus() == status)
                .filter(movement -> sourceId == null || movement.getSourceLocation() != null && sourceId.equals(movement.getSourceLocation().getId()))
                .filter(movement -> targetId == null || movement.getTargetLocation() != null && targetId.equals(movement.getTargetLocation().getId()))
                .filter(movement -> itemId == null || movement.getLines().stream().anyMatch(line -> itemId.equals(line.getItem().getId())))
                .filter(movement -> from == null || movement.getCreatedAt() == null || !movement.getCreatedAt().isBefore(from.atStartOfDay().toInstant(java.time.ZoneOffset.UTC)))
                .filter(movement -> to == null || movement.getCreatedAt() == null || movement.getCreatedAt().isBefore(to.plusDays(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC)))
                .map(this::toMovementResponse)
                .sorted(movementComparator(sort))
                .toList();
        return Result.success(page(result, page, size));
    }

    @Transactional(readOnly = true)
    public Result<MovementResponse> getMovement(UserContext user, Long id) {
        Result<MovementResponse> denied = permission(user, Permission.INVENTORY_MOVEMENT_READ);
        if (denied != null) return denied;
        return movements.findById(id).map(movement -> Result.success(toMovementResponse(movement)))
                .orElseGet(() -> Result.notFound("Inventory movement was not found"));
    }

    @Transactional(readOnly = true)
    public Result<ItemBalanceResponse> itemBalance(UserContext user, Long id) {
        Result<ItemBalanceResponse> denied = permission(user, Permission.INVENTORY_BALANCE_READ);
        if (denied != null) return denied;
        Optional<InventoryItem> found = items.findById(id);
        if (found.isEmpty()) return Result.notFound("Inventory item was not found");
        InventoryItem item = found.get();
        List<BalanceLine> result = locations.findAll().stream().map(location -> {
            Set<String> serialNumbers = serials(item, location);
            return new BalanceLine(location.getId(), location.getCode(), quantity(item, location, serialNumbers), new ArrayList<>(serialNumbers));
        }).filter(balance -> balance.quantity().signum() != 0 || !balance.serialNumbers().isEmpty()).toList();
        return Result.success(new ItemBalanceResponse(toItemResponse(item), result));
    }

    @Transactional(readOnly = true)
    public Result<LocationBalanceResponse> locationBalance(UserContext user, Long id) {
        Result<LocationBalanceResponse> denied = permission(user, Permission.INVENTORY_BALANCE_READ);
        if (denied != null) return denied;
        Optional<InventoryLocation> found = locations.findById(id);
        if (found.isEmpty()) return Result.notFound("Inventory location was not found");
        InventoryLocation location = found.get();
        List<ItemBalance> result = items.findAll().stream().map(item -> {
            Set<String> serialNumbers = serials(item, location);
            return new ItemBalance(item.getId(), item.getCode(), quantity(item, location, serialNumbers), new ArrayList<>(serialNumbers));
        }).filter(balance -> balance.quantity().signum() != 0 || !balance.serialNumbers().isEmpty()).toList();
        return Result.success(new LocationBalanceResponse(toLocationResponse(location), result));
    }

    private BigDecimal quantity(InventoryItem item, InventoryLocation location, Set<String> serialNumbers) {
        if (item.getTrackingMode() == TrackingMode.SERIALIZED) return BigDecimal.valueOf(serialNumbers.size());
        return balances().getOrDefault(key(item, location), BigDecimal.ZERO);
    }

    private Result<InventoryLocation> makeLocation(LocationRequest request) {
        if (request.type() == LocationType.PROJECT_SITE && request.projectId() == null) return Result.validation("Project site requires a project");
        if (request.type() == LocationType.WAREHOUSE && request.projectId() != null) return Result.validation("Warehouse cannot have a project");
        InventoryLocation location = new InventoryLocation();
        location.setCode(request.code());
        location.setName(request.name());
        location.setType(request.type());
        location.setAddress(request.address());
        if (request.projectId() != null) {
            var project = projects.findById(request.projectId());
            if (project.isEmpty()) return Result.notFound("Project was not found");
            location.setProject(project.get());
        }
        return Result.success(location);
    }

    private boolean validDirection(MovementRequest request) {
        return switch (request.type()) {
            case RECEIPT -> request.sourceLocationId() == null && request.targetLocationId() != null && request.adjustmentDirection() == null;
            case ISSUE, RETURN -> request.sourceLocationId() != null && request.targetLocationId() == null && request.adjustmentDirection() == null;
            case TRANSFER -> request.sourceLocationId() != null && request.targetLocationId() != null && request.adjustmentDirection() == null;
            case ADJUSTMENT -> sourceLocationId(request) != null
                    && request.targetLocationId() == null && request.adjustmentDirection() != null;
        };
    }

    private boolean validLocations(MovementRequest request) {
        Long sourceLocationId = sourceLocationId(request);
        return sourceLocationId == null || locations.existsById(sourceLocationId)
                ? request.targetLocationId() == null || locations.existsById(request.targetLocationId())
                : false;
    }

    private Long sourceLocationId(MovementRequest request) {
        return request.type() == MovementType.ADJUSTMENT ? request.locationId() : request.sourceLocationId();
    }

    private InventoryLocation findLocation(Long id) { return id == null ? null : locations.findById(id).orElse(null); }

    private boolean hasHistory(InventoryItem item) {
        return movements.findAll().stream().anyMatch(movement -> movement.getLines().stream().anyMatch(line -> line.getItem().getId().equals(item.getId())));
    }

    private void applyItem(InventoryItem item, ItemRequest request) {
        item.setCode(request.code());
        item.setName(request.name());
        item.setCategory(request.category());
        item.setUnit(request.unit());
        item.setTrackingMode(request.trackingMode());
    }

    private String normalizedSerial(String serial) { return serial == null ? null : serial.trim(); }
    private boolean validPage(int page, int size) { return page >= 1 && size >= 1 && size <= 100; }
    private String key(InventoryItem item, InventoryLocation location) { return item.getId() + ":" + (location == null ? 0 : location.getId()); }

    private <T> PageResponse<T> page(List<T> values, int number, int size) {
        int from = Math.min((number - 1) * size, values.size());
        int to = Math.min(from + size, values.size());
        return new PageResponse<>(values.subList(from, to), number, size, values.size(), (values.size() + size - 1) / size);
    }

    private Comparator<ItemResponse> itemComparator(String sort) {
        return comparator(sort, item -> item.id(), Map.of(
                "id", item -> item.id(),
                "code", item -> item.code(),
                "name", item -> item.name()));
    }

    private Comparator<LocationResponse> locationComparator(String sort) {
        return comparator(sort, location -> location.id(), Map.of(
                "id", location -> location.id(),
                "code", location -> location.code(),
                "name", location -> location.name()));
    }

    private Comparator<MovementResponse> movementComparator(String sort) {
        return comparator(sort, movement -> movement.id(), Map.of(
                "id", movement -> movement.id(),
                "type", movement -> movement.type().name(),
                "status", movement -> movement.status().name()));
    }

    private <T> Comparator<T> comparator(
            String sort, Function<T, Comparable<?>> defaultKey,
            Map<String, Function<T, Comparable<?>>> keys) {
        String value = sort == null || sort.isBlank() ? "id" : sort.trim();
        boolean descending = value.startsWith("-");
        if (descending) value = value.substring(1);
        if (value.contains(",")) {
            String[] parts = value.split(",", 2);
            value = parts[0];
            descending = "DESC".equalsIgnoreCase(parts[1].trim());
        }
        Function<T, Comparable<?>> key = keys.getOrDefault(value, defaultKey);
        Comparator<T> comparator = Comparator.comparing(key, Comparator.nullsLast(this::compareValues));
        return descending ? comparator.reversed() : comparator;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private int compareValues(Comparable left, Comparable right) {
        return left.compareTo(right);
    }

    private ItemResponse toItemResponse(InventoryItem item) { return new ItemResponse(item.getId(), item.getCode(), item.getName(), item.getCategory(), item.getUnit(), item.getTrackingMode(), item.isActive()); }
    private LocationResponse toLocationResponse(InventoryLocation location) { return new LocationResponse(location.getId(), location.getCode(), location.getName(), location.getType(), location.getProject() == null ? null : location.getProject().getId(), location.getAddress(), location.isActive()); }
    private MovementResponse toMovementResponse(InventoryMovement movement) { return new MovementResponse(movement.getId(), movement.getType(), movement.getStatus(), movement.getSourceLocation() == null ? null : movement.getSourceLocation().getId(), movement.getTargetLocation() == null ? null : movement.getTargetLocation().getId(), movement.getAdjustmentDirection(), movement.getLines().stream().map(line -> new LineResponse(line.getItem().getId(), line.getItem().getCode(), line.getQuantity(), line.getSerialNumber())).toList(), movement.getReversalOf() == null ? null : movement.getReversalOf().getId()); }
}
