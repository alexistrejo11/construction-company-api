package io.github.alexisTrejo11.construction.company.modules.user.features.updatecurrent;

import io.github.alexisTrejo11.construction.company.modules.user.shared.dto.UserProfileResponse;
import io.github.alexisTrejo11.construction.company.modules.user.shared.mapper.UserProfileMapper;
import io.github.alexisTrejo11.construction.company.modules.user.shared.persistence.UserRepository;
import io.github.alexisTrejo11.construction.company.shared.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateCurrentUserHandler {
    private final UserRepository userRepository;
    private final UserProfileMapper mapper;

    @Transactional
    public Result<UserProfileResponse> execute(String email, UpdateCurrentUserCommand command) {
        return userRepository.findByEmail(email)
            .map(user -> {
                user.setFirstName(command.firstName());
                user.setLastName(command.lastName());
                user.setPhone(command.phone());
                return Result.success(mapper.toResponse(user));
            })
            .orElseGet(() -> Result.notFound("Current user was not found"));
    }
}
