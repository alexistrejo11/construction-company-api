package io.github.alexisTrejo11.construction.company.modules.project.phases.features.update;

import io.github.alexisTrejo11.construction.company.modules.project.phases.features.create.CreateProjectPhaseCommand;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.CurrentUser;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/v2/api/projects/{projectId}/phases")
@RequiredArgsConstructor
public class UpdateProjectPhaseController {
  private final UpdateProjectPhaseHandler handler;

  @PatchMapping("/{phaseId}")
  public ResponseEntity<ResponseWrapper<?>> updateProjectPhase(
      @CurrentUser UserContext user,
      @PathVariable Long projectId,
      @PathVariable Long phaseId,
      @RequestBody @Valid CreateProjectPhaseCommand request) {
    Result<?> result = handler.execute(user, projectId, phaseId, request);
    if (!result.isSuccess()) {
      return AppErrorResolver.handleResult(result);
    }

    return ResponseEntity.ok(ResponseWrapper.success(result.getData(), "Project phase successfully updated"));
  }
}
