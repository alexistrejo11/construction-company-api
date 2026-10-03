package io.github.alexisTrejo11.construction.company.modules.notification.features.readall;

import io.github.alexisTrejo11.construction.company.modules.notification.shared.dto.ReadAllResponse;
import io.github.alexisTrejo11.construction.company.modules.notification.shared.persistence.NotificationRepository;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarkAllNotificationsReadHandler {
    private final NotificationRepository repository;

    @Transactional
    public Result<ReadAllResponse> execute(UserContext user) {
        if (user == null) return Result.unauthenticated("Authentication is required");
        int count = repository.markUnreadAsRead(user.userId(), Instant.now());
        return Result.success(new ReadAllResponse(count));
    }
}
