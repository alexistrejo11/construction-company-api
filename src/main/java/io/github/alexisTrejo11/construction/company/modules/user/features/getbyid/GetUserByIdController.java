package io.github.alexisTrejo11.construction.company.modules.user.features.getbyid;

import io.github.alexisTrejo11.construction.company.modules.user.shared.dto.UserProfileResponse;
import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.CurrentUser;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/api/users")
public class GetUserByIdController {
    private final GetUserByIdHandler handler;

    public GetUserByIdController(GetUserByIdHandler handler) {
        this.handler = handler;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ResponseWrapper<?>> get(
        @CurrentUser UserContext user,
        @PathVariable Long userId
    ) {
        Result<UserProfileResponse> result = handler.execute(user, userId);

        if (!result.isSuccess()) {
            return AppErrorResolver.handleResult(result);
        }

        return ResponseEntity.ok(ResponseWrapper.success(result.getData(), "User fetched successfully"));
    }
}
