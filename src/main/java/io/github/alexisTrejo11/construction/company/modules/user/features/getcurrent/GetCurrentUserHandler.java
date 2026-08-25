package io.github.alexisTrejo11.construction.company.modules.user.features.getcurrent;

import io.github.alexisTrejo11.construction.company.modules.user.shared.dto.UserProfileResponse;
import io.github.alexisTrejo11.construction.company.modules.user.shared.mapper.UserProfileMapper;
import io.github.alexisTrejo11.construction.company.modules.user.shared.persistence.UserRepository;
import io.github.alexisTrejo11.construction.company.shared.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetCurrentUserHandler {
    private final UserRepository userRepository;
    private final UserProfileMapper mapper;

    public Result<UserProfileResponse> execute(String email) {
        return userRepository.findByEmail(email)
            .map(mapper::toResponse)
            .map(Result::success)
            .orElseGet(() -> Result.notFound("Current user was not found"));
    }
}
