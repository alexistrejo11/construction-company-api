package io.github.alexisTrejo11.construction.company.modules.project.phases.features.reorder;

import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.CurrentUser;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/v2/api/projects/{projectId}/phases")
@RequiredArgsConstructor
public class ReorderProjectPhasesController {
  private final ReorderProjectPhasesHandler handler;

  @PatchMapping("/reorder")
  public ResponseEntity<ResponseWrapper<?>> reorderProjectPhases(
      @CurrentUser UserContext user,
      @PathVariable Long projectId,
      @RequestBody @Valid ReorderProjectPhasesCommand request) {
    var result = handler.execute(user, projectId, request);
    if (!result.isSuccess()) {
      return AppErrorResolver.handleResult(result);
    }

    return ResponseEntity.ok(ResponseWrapper.success("Project phases successfully reordered"));
  }
}
