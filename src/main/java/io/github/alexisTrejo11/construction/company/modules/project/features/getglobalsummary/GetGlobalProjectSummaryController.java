package io.github.alexisTrejo11.construction.company.modules.project.features.getglobalsummary;

import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.CurrentUser;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/api/projects")
@RequiredArgsConstructor
public class GetGlobalProjectSummaryController {
    private final GetGlobalProjectSummaryHandler handler;

    @GetMapping("/summary")
    public ResponseEntity<ResponseWrapper<?>> getSummary(@CurrentUser UserContext user) {
        var result = handler.execute(user);
        if (!result.isSuccess()) {
            return AppErrorResolver.handleResult(result);
        }

        return ResponseEntity.ok(ResponseWrapper.found(result.getData(), "Projects summary"));
    }
}
