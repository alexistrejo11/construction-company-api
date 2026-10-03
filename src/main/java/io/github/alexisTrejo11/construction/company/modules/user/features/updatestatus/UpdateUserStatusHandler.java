package io.github.alexisTrejo11.construction.company.modules.user.features.updatestatus;

import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.UserStatus;
import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.User;
import io.github.alexisTrejo11.construction.company.modules.user.shared.dto.UserProfileResponse;
import io.github.alexisTrejo11.construction.company.modules.user.shared.mapper.UserProfileMapper;
import io.github.alexisTrejo11.construction.company.modules.user.shared.persistence.UserRepository;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.GlobalAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateUserStatusHandler {
    private final UserRepository userRepository;
    private final UserProfileMapper mapper;
    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;
    private final GlobalAuthorizationPolicy authorizationPolicy;

    @Transactional
    public Result<UserProfileResponse> execute(UserContext actor, Long userId, UpdateUserStatusCommand command) {
        Result<Void> authorization = authorizationPolicy.requirePermission(actor, Permission.USER_STATUS_MANAGE);
        if (!authorization.isSuccess()) {
            return Result.forbidden(authorization.getErrorMessage());
        }

        return userRepository.findById(userId)
            .map(user -> update(user, command))
            .orElseGet(() -> Result.notFound("User was not found"));
    }

    private Result<UserProfileResponse> update(
        User user,
        UpdateUserStatusCommand command
    ) {
        if (user.getStatus() == UserStatus.DISABLED
            && command.status() != UserStatus.DISABLED) {
            return Result.business("Disabled users cannot be reactivated");
        }

        if (user.getStatus() == UserStatus.INVITED) {
            return Result.business("Invited users can only be activated by accepting their invitation");
        }

        user.setStatus(command.status());

        if (command.status() == UserStatus.SUSPENDED
            || command.status() == UserStatus.DISABLED) {
            invalidateSessions(user.getEmail());
        }

        return Result.success(mapper.toResponse(user));
    }

    private void invalidateSessions(String email) {
        sessionRepository.findByPrincipalName(email)
            .keySet()
            .forEach(sessionRepository::deleteById);
    }
}
