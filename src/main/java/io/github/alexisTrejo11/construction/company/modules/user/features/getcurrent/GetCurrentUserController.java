package io.github.alexisTrejo11.construction.company.modules.user.features.getcurrent;

import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/api/users")
public class GetCurrentUserController {
    private final GetCurrentUserHandler handler;

    public GetCurrentUserController(GetCurrentUserHandler handler) {
        this.handler = handler;
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseWrapper<?>> get(Authentication authentication) {
        var result = handler.execute(authentication.getName());

        if (!result.isSuccess()) {
            return AppErrorResolver.handleResult(result);
        }

        return ResponseEntity.ok(ResponseWrapper.success(result.getData(), "Current user fetched successfully"));
    }
}
