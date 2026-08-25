package io.github.alexisTrejo11.construction.company.modules.project.members.features.get;

import io.github.alexisTrejo11.construction.company.modules.project.shared.dto.ProjectMemberResponse;
import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.AppErrorResolver;
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
@RequestMapping("/v2/api/projects/{projectId}/members")
@RequiredArgsConstructor
public class GetProjectMembersController {
  private final GetProjectMembersHandler handler;

  @GetMapping
  public ResponseEntity<ResponseWrapper<?>> getProjectMembers(
      @CurrentUser UserContext user,
      @PathVariable Long projectId) {
    var result = handler.execute(user, new GetProjectMembersQuery(projectId));
    if (!result.isSuccess()) {
      return AppErrorResolver.handleResult(result);
    }

    return ResponseEntity.ok(ResponseWrapper.found(result.getData(), "ProjectMember"));
  }
}
