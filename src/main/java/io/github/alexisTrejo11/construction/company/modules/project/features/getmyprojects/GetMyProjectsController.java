package io.github.alexisTrejo11.construction.company.modules.project.features.getmyprojects;

import io.github.alexisTrejo11.construction.company.modules.project.shared.dto.ProjectResponse;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.dto.PageRequest;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.CurrentUser;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import io.github.alexisTrejo11.construction.company.shared.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/api/projects")
@RequiredArgsConstructor
public class GetMyProjectsController {
  private final GetMyProjectsHandler handler;

  @GetMapping("/my-projects")
  public ResponseEntity<ResponseWrapper<?>> getMyProjects(
      @CurrentUser UserContext userContext,
      @ModelAttribute @Valid PageRequest request) {
    if (userContext == null || userContext.userId() == null) {
      return ResponseEntity.status(401).body(
          ResponseWrapper.failure(
              "Authentication is required",
              Result.ErrorType.UNAUTHENTICATED,
              "AUTHENTICATION_REQUIRED",
              null
          )
      );
    }

    Result<PageResponse<ProjectResponse>> result = handler.execute(
        userContext,
        new GetMyProjectsQuery(userContext.userId(), request)
    );
    if (!result.isSuccess()) {
      return AppErrorResolver.handleResult(result);
    }

    return ResponseEntity.ok(ResponseWrapper.found(result.getData(), "Project"));
  }
}
