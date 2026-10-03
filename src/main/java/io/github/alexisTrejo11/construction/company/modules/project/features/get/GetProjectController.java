package io.github.alexisTrejo11.construction.company.modules.project.features.get;

import io.github.alexisTrejo11.construction.company.modules.project.shared.dto.ProjectResponse;
import io.github.alexisTrejo11.construction.company.modules.project.shared.domain.ProjectStatus;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.CurrentUser;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import io.github.alexisTrejo11.construction.company.shared.dto.PageResponse;
import io.github.alexisTrejo11.construction.company.shared.dto.PageRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/api/projects")
@RequiredArgsConstructor
public class GetProjectController {
  private final GetProjectHandler handler;

  @GetMapping
  public ResponseEntity<ResponseWrapper<?>> getProjects(
      @CurrentUser UserContext user,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) ProjectStatus status,
      @RequestParam(required = false) String city,
      @RequestParam(defaultValue = "1") @Min(1) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "ASC") String sortDirection) {
    Result<PageResponse<ProjectResponse>> result = handler.execute(
        user,
        new GetProjectQuery(
            search,
            status,
            city,
            new PageRequest(
                page,
                size,
                sortBy,
                sortDirection
            )
        )
    );
    if (!result.isSuccess()) {
      return AppErrorResolver.handleResult(result);
    }

    return ResponseEntity.ok(ResponseWrapper.found(result.getData(), "Project"));
  }
}
