package io.github.alexisTrejo11.construction.company.modules.user.features.acceptinvitation;

import io.github.alexisTrejo11.construction.company.modules.user.shared.dto.UserProfileResponse;
import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.Result;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/api/invitations")
public class AcceptInvitationController {
    private final AcceptInvitationHandler handler;

    public AcceptInvitationController(AcceptInvitationHandler handler) {
        this.handler = handler;
    }

    @PostMapping("/{token}/accept")
    public ResponseEntity<ResponseWrapper<?>> accept(
        @PathVariable String token,
        @Valid @RequestBody AcceptInvitationCommand command
    ) {
        Result<UserProfileResponse> result = handler.execute(token, command);
        if (!result.isSuccess()) {
            return AppErrorResolver.handleResult(result);
        }

        UserProfileResponse userProfileResponse = result.getData();
        var responseBody = ResponseWrapper.success(userProfileResponse, "Invitation accepted successfully");
        return ResponseEntity.ok(responseBody);
    }
}
