package io.github.alexisTrejo11.construction.company.modules.notification.features.invitation;

import io.github.alexisTrejo11.construction.company.modules.user.features.createinvitation.InvitationCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class InvitationNotificationListener {
    private final InvitationNotificationPersistence persistence;
    private final InvitationEmailDelivery emailDelivery;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(InvitationCreatedEvent event) {
        persistence.persist(event);
        emailDelivery.send(event);
    }
}
