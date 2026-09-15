package io.github.alexisTrejo11.construction.company.modules.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.alexisTrejo11.construction.company.modules.notification.features.get.GetNotificationHandler;
import io.github.alexisTrejo11.construction.company.modules.notification.features.list.ListNotificationsHandler;
import io.github.alexisTrejo11.construction.company.modules.notification.features.list.ListNotificationsQuery;
import io.github.alexisTrejo11.construction.company.modules.notification.shared.persistence.NotificationRepository;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationHandlerResultTest {
    @Mock private NotificationRepository repository;

    @Test
    void getReturnsNotFoundWhenNotificationDoesNotBelongToUser() {
        UserContext user = new UserContext(10L, "owner@example.com", java.util.Set.of(), java.util.Set.of());
        when(repository.findByIdAndRecipientId(3L, 10L)).thenReturn(Optional.empty());

        Result<?> result = new GetNotificationHandler(repository).execute(user, 3L);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorType()).isEqualTo(Result.ErrorType.NOT_FOUND);
        verify(repository).findByIdAndRecipientId(3L, 10L);
    }

    @Test
    void listRejectsInvalidPaginationBeforeCallingRepository() {
        UserContext user = new UserContext(10L, "owner@example.com", java.util.Set.of(), java.util.Set.of());

        Result<?> result = new ListNotificationsHandler(repository)
                .execute(user, new ListNotificationsQuery(null, 0, 20, null));

        assertThat(result.getErrorType()).isEqualTo(Result.ErrorType.VALIDATION);
        org.mockito.Mockito.verifyNoInteractions(repository);
    }
}
