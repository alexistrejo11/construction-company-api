package io.github.alexisTrejo11.construction.company.modules.user.features.createinvitation;

public record InvitationCreatedEvent(
    Long invitationId,
    Long recipientUserId,
    String recipientEmail,
    String rawToken
) {
}
