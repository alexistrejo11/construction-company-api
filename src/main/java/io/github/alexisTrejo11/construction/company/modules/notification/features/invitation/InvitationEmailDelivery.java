package io.github.alexisTrejo11.construction.company.modules.notification.features.invitation;

import io.github.alexisTrejo11.construction.company.modules.user.features.createinvitation.InvitationCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvitationEmailDelivery {
    private final JavaMailSender mailSender;

    @Async("taskExecutor")
    public void send(InvitationCreatedEvent event) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(event.recipientEmail());
            message.setSubject("Construction company invitation");
            message.setText("Invitation token: " + event.rawToken());
            mailSender.send(message);
        } catch (RuntimeException exception) {
            log.warn("Unable to send invitation email to {}", event.recipientEmail(), exception);
        }
    }
}
