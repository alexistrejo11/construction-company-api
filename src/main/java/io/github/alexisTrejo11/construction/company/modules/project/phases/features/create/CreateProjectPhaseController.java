package io.github.alexisTrejo11.construction.company.modules.project.phases.features.create;

import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.Result;
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
@RequestMapping("/v2/api/projects/{projectId}/phases")
@RequiredArgsConstructor
public class CreateProjectPhaseController {
  private final CreateProjectPhaseHandler handler;

  @PostMapping
  public ResponseEntity<ResponseWrapper<?>> addProjectPhase(
      @CurrentUser UserContext user,
      @PathVariable Long projectId,
      @RequestBody @Valid CreateProjectPhaseCommand request) {
    Result<?> result = handler.execute(user, projectId, request);
    if (!result.isSuccess()) {
      return AppErrorResolver.handleResult(result);
    }

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ResponseWrapper.created(result.getData(), "ProjectPhase"));
  }
}
