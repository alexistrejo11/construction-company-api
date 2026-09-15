package io.github.alexisTrejo11.construction.company.modules.notification.features.read;

import io.github.alexisTrejo11.construction.company.modules.notification.shared.dto.NotificationResponse;
import io.github.alexisTrejo11.construction.company.modules.notification.shared.persistence.NotificationRepository;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarkNotificationReadHandler {
    private final NotificationRepository repository;

    @Transactional
    public Result<NotificationResponse> execute(UserContext user, Long id) {
        if (user == null) return Result.unauthenticated("Authentication is required");
        return repository.findByIdAndRecipientId(id, user.userId())
            .map(notification -> {
                notification.markRead();
                return Result.success(NotificationResponse.from(notification));
            })
            .orElseGet(() -> Result.notFound("Notification not found"));
    }
}
