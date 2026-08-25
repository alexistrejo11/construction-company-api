package io.github.alexisTrejo11.construction.company.modules.auth.features.logout;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/api/auth")
public class LogoutController {
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) throws Exception {
        request.logout();

        return ResponseEntity.noContent().build();
    }
}
