package io.github.alexisTrejo11.construction.company.modules.inventory.features;

import io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain.InventoryCategory;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain.LocationType;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain.MovementStatus;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain.MovementType;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain.TrackingMode;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.dto.InventoryDtos.ItemRequest;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.dto.InventoryDtos.ItemStatusRequest;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.dto.InventoryDtos.LocationRequest;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.dto.InventoryDtos.LocationStatusRequest;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.dto.InventoryDtos.MovementRequest;
import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.CurrentUser;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryHandler handler;

    @PostMapping("/items")
    public ResponseEntity<ResponseWrapper<?>> createItem(@CurrentUser UserContext user, @Valid @RequestBody ItemRequest request) {
        return response(handler.createItem(user, request), HttpStatus.CREATED, "InventoryItem created successfully");
    }

    @GetMapping("/items")
    public ResponseEntity<ResponseWrapper<?>> listItems(@CurrentUser UserContext user, @RequestParam(required = false) String search,
            @RequestParam(required = false) InventoryCategory category, @RequestParam(required = false) TrackingMode trackingMode,
            @RequestParam(required = false) Boolean active, @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size, @RequestParam(defaultValue = "id,asc") String sort) {
        return response(handler.listItems(user, search, category, trackingMode, active, page, size, sort), HttpStatus.OK, "InventoryItem fetched successfully");
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<ResponseWrapper<?>> getItem(@CurrentUser UserContext user, @PathVariable Long id) {
        return response(handler.getItem(user, id), HttpStatus.OK, "InventoryItem fetched successfully");
    }

    @PatchMapping("/items/{id}")
    public ResponseEntity<ResponseWrapper<?>> updateItem(@CurrentUser UserContext user, @PathVariable Long id, @Valid @RequestBody ItemRequest request) {
        return response(handler.updateItem(user, id, request), HttpStatus.OK, "InventoryItem updated successfully");
    }

    @PatchMapping("/items/{id}/status")
    public ResponseEntity<ResponseWrapper<?>> itemStatus(@CurrentUser UserContext user, @PathVariable Long id, @Valid @RequestBody ItemStatusRequest request) {
        return response(handler.itemStatus(user, id, request), HttpStatus.OK, "InventoryItem status updated successfully");
    }

    @GetMapping("/items/{id}/balance")
    public ResponseEntity<ResponseWrapper<?>> itemBalance(@CurrentUser UserContext user, @PathVariable Long id) {
        return response(handler.itemBalance(user, id), HttpStatus.OK, "Inventory balance fetched successfully");
    }

    @PostMapping("/locations")
    public ResponseEntity<ResponseWrapper<?>> createLocation(@CurrentUser UserContext user, @Valid @RequestBody LocationRequest request) {
        return response(handler.createLocation(user, request), HttpStatus.CREATED, "InventoryLocation created successfully");
    }

    @GetMapping("/locations")
    public ResponseEntity<ResponseWrapper<?>> listLocations(@CurrentUser UserContext user, @RequestParam(required = false) String search,
            @RequestParam(required = false) LocationType type, @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Boolean active, @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size, @RequestParam(defaultValue = "id,asc") String sort) {
        return response(handler.listLocations(user, search, type, projectId, active, page, size, sort), HttpStatus.OK, "InventoryLocation fetched successfully");
    }

    @GetMapping("/locations/{id}")
    public ResponseEntity<ResponseWrapper<?>> getLocation(@CurrentUser UserContext user, @PathVariable Long id) {
        return response(handler.getLocation(user, id), HttpStatus.OK, "InventoryLocation fetched successfully");
    }

    @PatchMapping("/locations/{id}")
    public ResponseEntity<ResponseWrapper<?>> updateLocation(@CurrentUser UserContext user, @PathVariable Long id, @Valid @RequestBody LocationRequest request) {
        return response(handler.updateLocation(user, id, request), HttpStatus.OK, "InventoryLocation updated successfully");
    }

    @PatchMapping("/locations/{id}/status")
    public ResponseEntity<ResponseWrapper<?>> locationStatus(@CurrentUser UserContext user, @PathVariable Long id, @Valid @RequestBody LocationStatusRequest request) {
        return response(handler.locationStatus(user, id, request), HttpStatus.OK, "InventoryLocation status updated successfully");
    }

    @GetMapping("/locations/{id}/balance")
    public ResponseEntity<ResponseWrapper<?>> locationBalance(@CurrentUser UserContext user, @PathVariable Long id) {
        return response(handler.locationBalance(user, id), HttpStatus.OK, "Inventory balance fetched successfully");
    }

    @PostMapping("/movements")
    public ResponseEntity<ResponseWrapper<?>> createMovement(@CurrentUser UserContext user, @Valid @RequestBody MovementRequest request) {
        return response(handler.createMovement(user, normalizeAdjustment(request)), HttpStatus.CREATED, "InventoryMovement created successfully");
    }

    @GetMapping("/movements")
    public ResponseEntity<ResponseWrapper<?>> listMovements(@CurrentUser UserContext user, @RequestParam(required = false) MovementType type,
            @RequestParam(required = false) MovementStatus status, @RequestParam(required = false) Long sourceLocationId,
            @RequestParam(required = false) Long targetLocationId, @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) LocalDate from, @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {
        return response(handler.listMovements(user, type, status, sourceLocationId, targetLocationId, itemId, from, to, page, size, sort), HttpStatus.OK, "InventoryMovement fetched successfully");
    }

    @GetMapping("/movements/{id}")
    public ResponseEntity<ResponseWrapper<?>> getMovement(@CurrentUser UserContext user, @PathVariable Long id) {
        return response(handler.getMovement(user, id), HttpStatus.OK, "InventoryMovement fetched successfully");
    }

    @PatchMapping("/movements/{id}")
    public ResponseEntity<ResponseWrapper<?>> updateMovement(@CurrentUser UserContext user, @PathVariable Long id, @Valid @RequestBody MovementRequest request) {
        return response(handler.updateMovement(user, id, normalizeAdjustment(request)), HttpStatus.OK, "InventoryMovement updated successfully");
    }

    @PostMapping("/movements/{id}/post")
    public ResponseEntity<ResponseWrapper<?>> postMovement(@CurrentUser UserContext user, @PathVariable Long id) {
        return response(handler.post(user, id), HttpStatus.OK, "InventoryMovement posted successfully");
    }

    @PostMapping("/movements/{id}/reverse")
    public ResponseEntity<ResponseWrapper<?>> reverseMovement(@CurrentUser UserContext user, @PathVariable Long id) {
        return response(handler.reverse(user, id), HttpStatus.CREATED, "InventoryMovement created successfully");
    }

    private MovementRequest normalizeAdjustment(MovementRequest request) {
        if (request.type() == MovementType.ADJUSTMENT && request.sourceLocationId() == null) {
            return new MovementRequest(request.type(), request.locationId(), request.targetLocationId(), request.locationId(), request.adjustmentDirection(), request.lines());
        }
        return request;
    }

    private ResponseEntity<ResponseWrapper<?>> response(Result<?> result, HttpStatus success, String message) {
        if (result.isSuccess()) return ResponseEntity.status(success).body(ResponseWrapper.success(result.getData(), message));
        return AppErrorResolver.handleResult(result);
    }
}
