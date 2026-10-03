package io.github.alexisTrejo11.construction.company.modules.notification.features.list;

import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.CurrentUser;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/api/notifications")
@RequiredArgsConstructor
public class ListNotificationsController {
    private final ListNotificationsHandler handler;

    @GetMapping
    public ResponseEntity<ResponseWrapper<?>> list(
        @CurrentUser UserContext user,
        @RequestParam(required = false) Boolean read,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String sort
    ) {
        var result = handler.execute(user, new ListNotificationsQuery(read, page, size, sort));
        if (!result.isSuccess()) {
            return AppErrorResolver.handleResult(result);
        }
        return ResponseEntity.ok(ResponseWrapper.found(result.getData(), "Notifications"));
    }
}
