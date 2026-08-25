package io.github.alexisTrejo11.construction.company.modules.user.shared.mapper;

import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.User;
import io.github.alexisTrejo11.construction.company.modules.user.shared.dto.UserProfileResponse;
import org.springframework.stereotype.Component;

@Component
public class UserProfileMapper {
    public UserProfileResponse toResponse(User user) {
        return new UserProfileResponse(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getPhone(),
            user.getStatus(),
            user.getRoles()
        );
    }
}
