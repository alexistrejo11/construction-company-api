package io.github.alexisTrejo11.construction.company.modules.project.features.updatestatus;

import io.github.alexisTrejo11.construction.company.modules.project.shared.dto.ProjectResponse;
import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.CurrentUser;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/api/projects")
@RequiredArgsConstructor
public class UpdateProjectStatusController {
  private final UpdateProjectStatusHandler handler;

  @PatchMapping("/{projectId}/status")
  public ResponseEntity<ResponseWrapper<?>> updateProjectStatus(
      @CurrentUser UserContext user,
      @PathVariable Long projectId,
      @RequestBody @Valid UpdateProjectStatusCommand request) {
    Result<ProjectResponse> projectResult = handler.execute(user, projectId, request);
    if (!projectResult.isSuccess()) {
      return AppErrorResolver.handleResult(projectResult);
    }

    return ResponseEntity.ok(ResponseWrapper.success(projectResult.getData(), "Project status successfully updated"));
  }
}
