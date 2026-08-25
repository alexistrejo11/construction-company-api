package io.github.alexisTrejo11.construction.company.modules.user.features.getbyid;

import io.github.alexisTrejo11.construction.company.modules.user.shared.dto.UserProfileResponse;
import io.github.alexisTrejo11.construction.company.modules.user.shared.mapper.UserProfileMapper;
import io.github.alexisTrejo11.construction.company.modules.user.shared.persistence.UserRepository;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.GlobalAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetUserByIdHandler {
    private final UserRepository userRepository;
    private final UserProfileMapper mapper;
    private final GlobalAuthorizationPolicy authorizationPolicy;

    public Result<UserProfileResponse> execute(UserContext user, Long userId) {
        Result<Void> authorization = authorizationPolicy.requirePermission(user, Permission.USER_READ);
        if (!authorization.isSuccess()) {
            return Result.forbidden(authorization.getErrorMessage());
        }

        return userRepository.findById(userId)
            .map(mapper::toResponse)
            .map(Result::success)
            .orElseGet(() -> Result.notFound("User was not found"));
    }
}
