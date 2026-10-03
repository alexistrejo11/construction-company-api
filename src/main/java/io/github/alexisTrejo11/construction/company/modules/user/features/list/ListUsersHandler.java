package io.github.alexisTrejo11.construction.company.modules.user.features.list;

import io.github.alexisTrejo11.construction.company.modules.user.shared.dto.UserProfileResponse;
import io.github.alexisTrejo11.construction.company.modules.user.shared.mapper.UserProfileMapper;
import io.github.alexisTrejo11.construction.company.modules.user.shared.persistence.UserRepository;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.GlobalAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListUsersHandler {
    private final UserRepository userRepository;
    private final UserProfileMapper mapper;
    private final GlobalAuthorizationPolicy authorizationPolicy;

    public Result<List<UserProfileResponse>> execute(UserContext user) {
        Result<Void> authorization = authorizationPolicy.requirePermission(user, Permission.USER_READ);
        if (!authorization.isSuccess()) {
            return Result.forbidden(authorization.getErrorMessage());
        }

        return Result.success(userRepository.findAll().stream()
            .map(mapper::toResponse)
            .toList());
    }
}
