package io.github.alexisTrejo11.construction.company.modules.project.phases.features.update;

import io.github.alexisTrejo11.construction.company.modules.project.phases.features.create.CreateProjectPhaseCommand;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/api/projects/{projectId}/phases")
@RequiredArgsConstructor
public class UpdateProjectPhaseController {
  private final UpdateProjectPhaseHandler handler;

  @PatchMapping("/{phaseId}")
  public ResponseWrapper<?> updateProjectPhase(
      @PathVariable Long projectId,
      @PathVariable Long phaseId,
      @RequestBody @Valid CreateProjectPhaseCommand request) {
    return ResponseWrapper.success(handler.execute(projectId, phaseId, request), "Project phase successfully updated");
  }
}
