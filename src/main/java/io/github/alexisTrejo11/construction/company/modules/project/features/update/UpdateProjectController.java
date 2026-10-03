package io.github.alexisTrejo11.construction.company.modules.project.features.update;

import io.github.alexisTrejo11.construction.company.modules.project.shared.dto.ProjectResponse;
import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.CurrentUser;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/api/projects")
@RequiredArgsConstructor
public class UpdateProjectController {
  private final UpdateProjectHandler handler;

  @PatchMapping("/{projectId}")
  public ResponseEntity<ResponseWrapper<?>> updateProject(
      @CurrentUser UserContext user,
      @PathVariable Long projectId,
      @RequestBody @Valid UpdateProjectCommand request) {
    Result<ProjectResponse> projectResult = handler.execute(user, projectId, request);
    if (!projectResult.isSuccess()) {
      return AppErrorResolver.handleResult(projectResult);
    }

    return ResponseEntity.ok(ResponseWrapper.success(projectResult.getData(), "Project successfully updated"));
  }
}
