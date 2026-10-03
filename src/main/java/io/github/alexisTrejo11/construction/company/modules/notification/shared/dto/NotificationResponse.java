package io.github.alexisTrejo11.construction.company.modules.notification.shared.dto;

import io.github.alexisTrejo11.construction.company.modules.notification.shared.domain.Notification;
import io.github.alexisTrejo11.construction.company.modules.notification.shared.domain.NotificationType;
import java.time.Instant;

public record NotificationResponse(
    Long id,
    NotificationType type,
    String title,
    String message,
    String resourceType,
    Long resourceId,
    Instant readAt,
    Instant createdAt,
    Instant updatedAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
            notification.getId(), notification.getType(), notification.getTitle(), notification.getMessage(),
            notification.getResourceType(), notification.getResourceId(), notification.getReadAt(),
            notification.getCreatedAt(), notification.getUpdatedAt());
    }
}
