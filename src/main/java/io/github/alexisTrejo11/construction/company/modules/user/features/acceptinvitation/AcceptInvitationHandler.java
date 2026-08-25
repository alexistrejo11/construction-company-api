package io.github.alexisTrejo11.construction.company.modules.user.features.acceptinvitation;

import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.InvitationStatus;
import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.UserStatus;
import io.github.alexisTrejo11.construction.company.modules.user.shared.dto.UserProfileResponse;
import io.github.alexisTrejo11.construction.company.modules.user.shared.mapper.UserProfileMapper;
import io.github.alexisTrejo11.construction.company.modules.user.shared.persistence.InvitationRepository;
import io.github.alexisTrejo11.construction.company.shared.Result;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AcceptInvitationHandler {
    private final InvitationRepository invitationRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileMapper mapper;

    @Transactional
    public Result<UserProfileResponse> execute(String token, AcceptInvitationCommand command) {
        return invitationRepository.findByTokenHash(hash(token))
            .map(invitation -> accept(invitation, command))
            .orElseGet(() -> Result.notFound("Invitation was not found"));
    }

    private Result<UserProfileResponse> accept(
        io.github.alexisTrejo11.construction.company.modules.user.shared.domain.Invitation invitation,
        AcceptInvitationCommand command
    ) {
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            return Result.business("Invitation is not valid");
        }

        if (invitation.getExpiresAt().isBefore(Instant.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            return Result.business("Invitation is not valid");
        }

        var user = invitation.getUser();
        user.setFirstName(command.firstName());
        user.setLastName(command.lastName());
        user.setPhone(command.phone());
        user.setPasswordHash(passwordEncoder.encode(command.password()));
        user.setStatus(UserStatus.ACTIVE);
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(Instant.now());

        return Result.success(mapper.toResponse(user));
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
