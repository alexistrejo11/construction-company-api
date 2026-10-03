package io.github.alexisTrejo11.construction.company.modules.auth.features.login;

import io.github.alexisTrejo11.construction.company.modules.user.shared.dto.UserProfileResponse;
import io.github.alexisTrejo11.construction.company.modules.user.shared.mapper.UserProfileMapper;
import io.github.alexisTrejo11.construction.company.modules.user.shared.persistence.UserRepository;
import io.github.alexisTrejo11.construction.company.shared.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.AuthenticationException;

@Service
@RequiredArgsConstructor
public class LoginHandler {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserProfileMapper mapper;

    public Result<UserProfileResponse> execute(LoginCommand command) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(command.email(), command.password())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            return userRepository.findByEmail(command.email())
                .map(mapper::toResponse)
                .map(Result::success)
                .orElseGet(() -> Result.unauthenticated("Invalid credentials"));
        } catch (AuthenticationException exception) {
            return Result.unauthenticated("Invalid credentials");
        }
    }
}
