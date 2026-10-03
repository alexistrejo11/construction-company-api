package io.github.alexisTrejo11.construction.company.modules.notification.features.invitation;

import io.github.alexisTrejo11.construction.company.modules.notification.shared.domain.Notification;
import io.github.alexisTrejo11.construction.company.modules.notification.shared.domain.NotificationType;
import io.github.alexisTrejo11.construction.company.modules.notification.shared.persistence.NotificationRepository;
import io.github.alexisTrejo11.construction.company.modules.user.features.createinvitation.InvitationCreatedEvent;
import io.github.alexisTrejo11.construction.company.modules.user.shared.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class InvitationNotificationPersistence {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void persist(InvitationCreatedEvent event) {
        userRepository.findById(event.recipientUserId()).ifPresent(user -> {
            Notification notification = new Notification();
            notification.setRecipient(user);
            notification.setType(NotificationType.INVITATION);
            notification.setTitle("You have been invited");
            notification.setMessage("You have received an invitation to join the construction company.");
            notification.setResourceType("INVITATION");
            notification.setResourceId(event.invitationId());
            notificationRepository.save(notification);
        });
    }
}
