package io.github.alexisTrejo11.construction.company.modules.user.features.updatecurrent;

import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/api/users")
public class UpdateCurrentUserController {
    private final UpdateCurrentUserHandler handler;

    public UpdateCurrentUserController(UpdateCurrentUserHandler handler) {
        this.handler = handler;
    }

    @PatchMapping("/me")
    public ResponseEntity<ResponseWrapper<?>> update(
        Authentication authentication,
        @Valid @RequestBody UpdateCurrentUserCommand command
    ) {
        var result = handler.execute(authentication.getName(), command);

        if (!result.isSuccess()) {
            return AppErrorResolver.handleResult(result);
        }

        return ResponseEntity.ok(ResponseWrapper.success(result.getData(), "Profile updated successfully"));
    }
}
