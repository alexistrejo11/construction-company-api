package io.github.alexisTrejo11.construction.company.modules.user.features.update;

import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.CurrentUser;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/api/users")
public class UpdateUserController {
    private final UpdateUserHandler handler;

    public UpdateUserController(UpdateUserHandler handler) {
        this.handler = handler;
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<ResponseWrapper<?>> update(
        @CurrentUser UserContext user,
        @PathVariable Long userId,
        @Valid @RequestBody UpdateUserCommand command
    ) {
        var result = handler.execute(user, userId, command);

        if (!result.isSuccess()) {
            return AppErrorResolver.handleResult(result);
        }

        return ResponseEntity.ok(ResponseWrapper.success(result.getData(), "User profile updated successfully"));
    }
}
