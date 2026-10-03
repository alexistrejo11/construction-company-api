package io.github.alexisTrejo11.construction.company.modules.user.features.updateroles;

import io.github.alexisTrejo11.construction.company.modules.user.shared.dto.UserProfileResponse;
import io.github.alexisTrejo11.construction.company.modules.user.shared.mapper.UserProfileMapper;
import io.github.alexisTrejo11.construction.company.modules.user.shared.persistence.UserRepository;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.GlobalAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import java.util.EnumSet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateUserRolesHandler {
    private final UserRepository userRepository;
    private final UserProfileMapper mapper;
    private final GlobalAuthorizationPolicy authorizationPolicy;

    @Transactional
    public Result<UserProfileResponse> execute(UserContext actor, Long userId, UpdateUserRolesCommand command) {
        Result<Void> authorization = authorizationPolicy.requirePermission(actor, Permission.USER_ROLE_MANAGE);
        if (!authorization.isSuccess()) {
            return Result.forbidden(authorization.getErrorMessage());
        }

        return userRepository.findById(userId)
            .map(user -> {
                user.setRoles(EnumSet.copyOf(command.roles()));
                return Result.success(mapper.toResponse(user));
            })
            .orElseGet(() -> Result.notFound("User was not found"));
    }
}
