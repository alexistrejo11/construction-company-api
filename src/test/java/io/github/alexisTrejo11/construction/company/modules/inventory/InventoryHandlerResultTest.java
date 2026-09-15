package io.github.alexisTrejo11.construction.company.modules.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.github.alexisTrejo11.construction.company.modules.inventory.features.InventoryHandler;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.persistence.InventoryItemRepository;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.persistence.InventoryLocationRepository;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.persistence.InventoryMovementRepository;
import io.github.alexisTrejo11.construction.company.modules.project.shared.persistence.ProjectRepository;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.DefaultGlobalAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import java.util.Set;
import org.junit.jupiter.api.Test;

class InventoryHandlerResultTest {
    @Test
    void itemReadReturnsForbiddenWhenUserHasNoGlobalInventoryPermission() {
        InventoryHandler handler = new InventoryHandler(
                mock(InventoryItemRepository.class),
                mock(InventoryLocationRepository.class),
                mock(InventoryMovementRepository.class),
                mock(ProjectRepository.class),
                new DefaultGlobalAuthorizationPolicy());

        Result<?> result = handler.listItems(
                new UserContext(1L, "operator@example.com", Set.of(), Set.of()),
                null, null, null, null, 1, 20, null);

        assertThat(result.getErrorType()).isEqualTo(Result.ErrorType.FORBIDDEN);
    }
}
