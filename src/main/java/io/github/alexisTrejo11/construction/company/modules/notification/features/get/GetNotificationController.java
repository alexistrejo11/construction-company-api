package io.github.alexisTrejo11.construction.company.modules.notification.features.get;

import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.CurrentUser;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/api/notifications")
@RequiredArgsConstructor
public class GetNotificationController {
    private final GetNotificationHandler handler;

    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<?>> get(@CurrentUser UserContext user, @PathVariable Long id) {
        var result = handler.execute(user, id);
        if (!result.isSuccess()) return AppErrorResolver.handleResult(result);
        return ResponseEntity.ok(ResponseWrapper.found(result.getData(), "Notification"));
    }
}
