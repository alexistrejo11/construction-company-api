package io.github.alexisTrejo11.construction.company.modules.user.features.update;

import io.github.alexisTrejo11.construction.company.modules.user.shared.dto.UserProfileResponse;
import io.github.alexisTrejo11.construction.company.modules.user.shared.mapper.UserProfileMapper;
import io.github.alexisTrejo11.construction.company.modules.user.shared.persistence.UserRepository;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.GlobalAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateUserHandler {
    private final UserRepository userRepository;
    private final UserProfileMapper mapper;
    private final GlobalAuthorizationPolicy authorizationPolicy;

    @Transactional
    public Result<UserProfileResponse> execute(UserContext actor, Long userId, UpdateUserCommand command) {
        Result<Void> authorization = authorizationPolicy.requirePermission(actor, Permission.USER_UPDATE);
        if (!authorization.isSuccess()) {
            return Result.forbidden(authorization.getErrorMessage());
        }

        return userRepository.findById(userId)
            .map(user -> {
                user.setFirstName(command.firstName());
                user.setLastName(command.lastName());
                user.setPhone(command.phone());
                return Result.success(mapper.toResponse(user));
            })
            .orElseGet(() -> Result.notFound("User was not found"));
    }
}
