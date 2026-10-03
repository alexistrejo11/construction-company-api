package io.github.alexisTrejo11.construction.company.modules.project.members.features.add;

import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.CurrentUser;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/api/projects/{projectId}/members")
@RequiredArgsConstructor
public class AddProjectMemberController {
  private final AddProjectMemberHandler handler;

  @PostMapping
  public ResponseEntity<ResponseWrapper<?>> addProjectMember(
      @CurrentUser UserContext user,
      @PathVariable Long projectId,
      @RequestBody @Valid AddProjectMemberCommand request) {
    var result = handler.execute(user, projectId, request);
    if (!result.isSuccess()) {
      return AppErrorResolver.handleResult(result);
    }

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ResponseWrapper.created(result.getData(), "ProjectMember"));
  }
}
