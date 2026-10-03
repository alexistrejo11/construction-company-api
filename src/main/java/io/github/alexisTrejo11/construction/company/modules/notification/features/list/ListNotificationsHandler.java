package io.github.alexisTrejo11.construction.company.modules.notification.features.list;

import io.github.alexisTrejo11.construction.company.modules.notification.shared.dto.NotificationResponse;
import io.github.alexisTrejo11.construction.company.modules.notification.shared.domain.Notification;
import io.github.alexisTrejo11.construction.company.modules.notification.shared.persistence.NotificationRepository;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.dto.PageResponse;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListNotificationsHandler {
    private final NotificationRepository repository;

    @Transactional(readOnly = true)
    public Result<PageResponse<NotificationResponse>> execute(UserContext user, ListNotificationsQuery query) {
        if (user == null || user.userId() == null) {
            return Result.unauthenticated("Authentication is required");
        }
        if (query.page() < 1 || query.size() < 1 || query.size() > 100) {
            return Result.validation("Page must be at least 1 and size must be between 1 and 100");
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if (query.sort() != null && !query.sort().isBlank()) {
            String value = query.sort().trim();
            Sort.Direction direction = Sort.Direction.DESC;
            if (value.startsWith("-")) {
                value = value.substring(1);
            } else if (value.contains(",")) {
                String[] parts = value.split(",", 2);
                value = parts[0];
                direction = Sort.Direction.fromOptionalString(parts[1]).orElse(null);
            } else if (value.contains(":")) {
                String[] parts = value.split(":", 2);
                value = parts[0];
                direction = Sort.Direction.fromOptionalString(parts[1]).orElse(null);
            }
            if (!"createdAt".equals(value) || direction == null) {
                return Result.validation("Sort must use createdAt with ASC or DESC direction");
            }
            sort = Sort.by(direction, value);
        }

        PageRequest pageable = PageRequest.of(query.page() - 1, query.size(), sort);
        Page<Notification> notifications =
            query.read() == null
                ? repository.findByRecipientId(user.userId(), pageable)
                : query.read()
                    ? repository.findByRecipientIdAndReadAtIsNotNull(user.userId(), pageable)
                    : repository.findByRecipientIdAndReadAtIsNull(user.userId(), pageable);

        return Result.success(new PageResponse<>(
            notifications.map(NotificationResponse::from).getContent(),
            query.page(), query.size(), notifications.getTotalElements(), notifications.getTotalPages()));
    }
}
