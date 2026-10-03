package io.github.alexisTrejo11.construction.company.modules.project.features.getbyid;

import io.github.alexisTrejo11.construction.company.modules.project.shared.dto.ProjectResponse;
import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.CurrentUser;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/api/projects")
@RequiredArgsConstructor
public class GetProjectByIdController {
  private final GetProjectByIdHandler handler;

  @GetMapping("/{projectId}")
  public ResponseEntity<ResponseWrapper<?>> getProjectById(
      @CurrentUser UserContext user,
      @PathVariable Long projectId) {
    Result<ProjectResponse> projectResult = handler.execute(user, new GetProjectByIdQuery(projectId));
    if (!projectResult.isSuccess()) {
      return AppErrorResolver.handleResult(projectResult);
    }

    return ResponseEntity.ok(ResponseWrapper.found(projectResult.getData(), "Project", "ID", projectId));
  }
}
