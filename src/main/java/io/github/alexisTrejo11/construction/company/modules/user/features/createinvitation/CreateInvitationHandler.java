package io.github.alexisTrejo11.construction.company.modules.user.features.createinvitation;

import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.Invitation;
import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.User;
import io.github.alexisTrejo11.construction.company.modules.user.shared.persistence.InvitationRepository;
import io.github.alexisTrejo11.construction.company.modules.user.shared.persistence.UserRepository;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.GlobalAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateInvitationHandler {
    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;
    private final JavaMailSender mailSender;
    private final GlobalAuthorizationPolicy authorizationPolicy;

    @Transactional
    public Result<Void> execute(UserContext inviter, CreateInvitationCommand command) {
        Result<Void> authorization = authorizationPolicy.requirePermission(inviter, Permission.USER_INVITE);
        if (!authorization.isSuccess()) {
            return authorization;
        }

        if (userRepository.findByEmail(command.email()).isPresent()) {
            return Result.conflict("A user with this email already exists");
        }

        User invitedUser = new User();
        invitedUser.setEmail(command.email());
        invitedUser.getRoles().addAll(command.roles());

        User inviterUser = userRepository.findById(inviter.userId()).orElse(null);
        User savedUser = userRepository.save(invitedUser);
        String rawToken = UUID.randomUUID() + UUID.randomUUID().toString();

        Invitation invitation = new Invitation();
        invitation.setUser(savedUser);
        invitation.setCreatedBy(inviterUser);
        invitation.setTokenHash(hash(rawToken));
        invitation.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        invitationRepository.save(invitation);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(command.email());
        message.setSubject("Construction company invitation");
        message.setText("Invitation token: " + rawToken);
        mailSender.send(message);

        return Result.success();
    }

    private String hash(String rawToken) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                .digest(rawToken.getBytes(StandardCharsets.UTF_8));

            return java.util.HexFormat.of().formatHex(hash);
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
