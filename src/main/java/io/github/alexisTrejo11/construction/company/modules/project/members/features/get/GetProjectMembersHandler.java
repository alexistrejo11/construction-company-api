package io.github.alexisTrejo11.construction.company.modules.project.members.features.get;

import io.github.alexisTrejo11.construction.company.modules.project.shared.dto.ProjectMemberResponse;
import io.github.alexisTrejo11.construction.company.modules.project.shared.policy.ProjectAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import io.github.alexisTrejo11.construction.company.modules.project.members.shared.persistence.ProjectMemberRepository;
import io.github.alexisTrejo11.construction.company.modules.project.shared.mapper.ProjectResponseMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetProjectMembersHandler {
  private final ProjectAuthorizationPolicy authorizationPolicy;
  private final ProjectMemberRepository memberRepository;
  private final ProjectResponseMapper mapper;

  public Result<List<ProjectMemberResponse>> execute(
      UserContext user,
      GetProjectMembersQuery query) {
    Result<Void> authorization = authorizationPolicy.requireProjectPermission(
        user,
        query.projectId(),
        Permission.MEMBER_READ
    );
    if (!authorization.isSuccess()) {
      return Result.error(authorization.getErrorType(), authorization.getErrorMessage());
    }

    return Result.success(memberRepository.findByProjectIdOrderByAssignedAtAsc(query.projectId())
        .stream()
        .map(mapper::toMemberResponse)
        .toList());
  }
}
