package io.github.alexisTrejo11.construction.company.modules.auth.features.me;

import io.github.alexisTrejo11.construction.company.modules.user.shared.dto.UserProfileResponse;
import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/api/auth")
public class GetAuthenticatedUserController {
    private final GetAuthenticatedUserHandler handler;

    public GetAuthenticatedUserController(GetAuthenticatedUserHandler handler) {
        this.handler = handler;
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseWrapper<?>> get(Authentication authentication) {
        Result<UserProfileResponse> result = handler.execute(authentication.getName());
        if (!result.isSuccess()) {
            return AppErrorResolver.handleResult(result);
        }

        var responseBody = ResponseWrapper.success(result.getData(), "Current user fetched successfully");
        return ResponseEntity.ok(responseBody);
    }
}
