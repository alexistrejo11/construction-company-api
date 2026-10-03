package io.github.alexisTrejo11.construction.company.modules.home.features.gethome;

import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GetHomeController {
    private final GetHomeHandler handler;

    public GetHomeController(GetHomeHandler handler) {
        this.handler = handler;
    }

    @GetMapping("/")
    public ResponseEntity<ResponseWrapper<?>> get() {
        Result<GetHomeResponse> result = handler.execute();
        if (!result.isSuccess()) {
            return AppErrorResolver.handleResult(result);
        }

        var responseBody = ResponseWrapper.success(result.getData(), "Welcome to the Construction Company API");
        return ResponseEntity.ok(responseBody);
    }
}
