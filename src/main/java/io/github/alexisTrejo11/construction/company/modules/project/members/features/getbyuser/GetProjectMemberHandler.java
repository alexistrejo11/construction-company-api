package io.github.alexisTrejo11.construction.company.modules.project.members.features.getbyuser;

import io.github.alexisTrejo11.construction.company.modules.project.members.shared.persistence.ProjectMemberRepository;
import io.github.alexisTrejo11.construction.company.modules.project.shared.dto.ProjectMemberResponse;
import io.github.alexisTrejo11.construction.company.modules.project.shared.mapper.ProjectResponseMapper;
import io.github.alexisTrejo11.construction.company.modules.project.shared.policy.ProjectAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetProjectMemberHandler {
    private final ProjectAuthorizationPolicy authorizationPolicy;
    private final ProjectMemberRepository memberRepository;
    private final ProjectResponseMapper mapper;

    public Result<ProjectMemberResponse> execute(
        UserContext actor,
        Long projectId,
        Long userId
    ) {
        Result<Void> authorization = authorizationPolicy.requireProjectPermission(
            actor,
            projectId,
            Permission.MEMBER_READ
        );
        if (!authorization.isSuccess()) {
            return Result.error(authorization.getErrorType(), authorization.getErrorMessage());
        }

        return memberRepository.findByProjectIdAndUserId(projectId, userId)
            .map(mapper::toMemberResponse)
            .map(Result::success)
            .orElseGet(() -> Result.notFound("Project member was not found"));
    }
}
