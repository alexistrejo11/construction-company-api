package io.github.alexisTrejo11.construction.company.modules.notification.features.read;

import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.CurrentUser;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/api/notifications")
@RequiredArgsConstructor
public class MarkNotificationReadController {
    private final MarkNotificationReadHandler handler;

    @PatchMapping("/{id}/read")
    public ResponseEntity<ResponseWrapper<?>> markRead(@CurrentUser UserContext user, @PathVariable Long id) {
        var result = handler.execute(user, id);
        if (!result.isSuccess()) return AppErrorResolver.handleResult(result);
        return ResponseEntity.ok(ResponseWrapper.success(result.getData(), "Notification marked as read"));
    }
}
