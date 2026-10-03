package io.github.alexisTrejo11.construction.company.modules.user.features.list;

import io.github.alexisTrejo11.construction.company.modules.user.shared.dto.UserProfileResponse;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.CurrentUser;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/api/users")
public class ListUsersController {
    private final ListUsersHandler handler;

    public ListUsersController(ListUsersHandler handler) {
        this.handler = handler;
    }

    @GetMapping
    public ResponseEntity<ResponseWrapper<?>> list(@CurrentUser UserContext user) {
        Result<List<UserProfileResponse>> result = handler.execute(user);
        if (!result.isSuccess()) {
            return AppErrorResolver.handleResult(result);
        }

        return ResponseEntity.ok(ResponseWrapper.success(result.getData(), "Users fetched successfully"));
    }
}
