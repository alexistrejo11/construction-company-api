package io.github.alexisTrejo11.construction.company.modules.project.phases.features.get;

import io.github.alexisTrejo11.construction.company.modules.project.shared.dto.ProjectPhaseResponse;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.CurrentUser;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/v2/api/projects/{projectId}/phases")
@RequiredArgsConstructor
public class GetProjectPhasesController {
  private final GetProjectPhasesHandler handler;

  @GetMapping
  public ResponseEntity<ResponseWrapper<?>> getProjectPhases(
      @CurrentUser UserContext user,
      @PathVariable Long projectId) {
    Result<List<ProjectPhaseResponse>> result = handler.execute(
        user,
        new GetProjectPhasesQuery(projectId)
    );
    if (!result.isSuccess()) {
      return AppErrorResolver.handleResult(result);
    }

    return ResponseEntity.ok(ResponseWrapper.found(result.getData(), "ProjectPhase"));
  }
}
