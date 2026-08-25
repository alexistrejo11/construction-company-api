package io.github.alexisTrejo11.construction.company.modules.auth.features.login;

import io.github.alexisTrejo11.construction.company.modules.user.shared.dto.UserProfileResponse;
import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.Result;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/api/auth")
public class LoginController {
    private final LoginHandler handler;

    public LoginController(LoginHandler handler) {
        this.handler = handler;
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseWrapper<?>> login(
        HttpServletRequest request,
        @Valid @RequestBody LoginCommand command
    ) {
        Result<UserProfileResponse> result = handler.execute(command);
        if (!result.isSuccess()) {
            return AppErrorResolver.handleResult(result);
        }

        request.getSession();
        request.changeSessionId();
        var responseBody = ResponseWrapper.success(result.getData(), "Authenticated");
        return ResponseEntity.ok(responseBody);
    }
}
