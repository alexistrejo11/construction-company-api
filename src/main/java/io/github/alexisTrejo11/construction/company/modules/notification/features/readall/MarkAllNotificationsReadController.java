package io.github.alexisTrejo11.construction.company.modules.notification.features.readall;

import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.CurrentUser;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/api/notifications")
@RequiredArgsConstructor
public class MarkAllNotificationsReadController {
    private final MarkAllNotificationsReadHandler handler;

    @PatchMapping("/read-all")
    public ResponseEntity<ResponseWrapper<?>> markAll(@CurrentUser UserContext user) {
        var result = handler.execute(user);
        if (!result.isSuccess()) return AppErrorResolver.handleResult(result);
        return ResponseEntity.ok(ResponseWrapper.success(result.getData(), "Notifications marked as read"));
    }
}
