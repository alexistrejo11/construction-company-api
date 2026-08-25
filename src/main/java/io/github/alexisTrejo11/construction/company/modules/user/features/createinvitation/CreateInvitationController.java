package io.github.alexisTrejo11.construction.company.modules.user.features.createinvitation;

import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.CurrentUser;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/api/invitations")
public class CreateInvitationController {
    private final CreateInvitationHandler handler;

    public CreateInvitationController(CreateInvitationHandler handler) {
        this.handler = handler;
    }

    @PostMapping
    public ResponseEntity<ResponseWrapper<?>> create(
        @CurrentUser UserContext user,
        @Valid @RequestBody CreateInvitationCommand command
    ) {
        Result<Void> result = handler.execute(user, command);

        if (!result.isSuccess()) {
            return AppErrorResolver.handleResult(result);
        }

        return ResponseEntity.status(201).body(ResponseWrapper.created(null, "Invitation"));
    }
}
