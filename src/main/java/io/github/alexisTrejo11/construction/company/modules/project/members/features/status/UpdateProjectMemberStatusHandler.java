package io.github.alexisTrejo11.construction.company.modules.project.members.features.status;

import io.github.alexisTrejo11.construction.company.modules.project.members.shared.domain.ProjectMemberStatus;
import io.github.alexisTrejo11.construction.company.modules.project.members.shared.persistence.ProjectMemberRepository;
import io.github.alexisTrejo11.construction.company.modules.project.shared.dto.ProjectMemberResponse;
import io.github.alexisTrejo11.construction.company.modules.project.shared.mapper.ProjectResponseMapper;
import io.github.alexisTrejo11.construction.company.modules.project.shared.policy.ProjectAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateProjectMemberStatusHandler {
    private final ProjectAuthorizationPolicy authorizationPolicy;
    private final ProjectMemberRepository memberRepository;
    private final ProjectResponseMapper mapper;

    @Transactional
    public Result<ProjectMemberResponse> execute(
        UserContext actor,
        Long projectId,
        Long userId,
        UpdateProjectMemberStatusCommand command
    ) {
        Result<Void> authorization = authorizationPolicy.requireProjectPermission(
            actor,
            projectId,
            Permission.MEMBER_MANAGE
        );
        if (!authorization.isSuccess()) {
            return Result.error(authorization.getErrorType(), authorization.getErrorMessage());
        }

        var member = memberRepository.findByProjectIdAndUserId(projectId, userId);
        if (member.isEmpty()) {
            return Result.notFound("Project member was not found");
        }

        member.get().setStatus(command.status());
        member.get().setDeactivatedAt(
            command.status() == ProjectMemberStatus.INACTIVE ? Instant.now() : null
        );
        return Result.success(mapper.toMemberResponse(memberRepository.save(member.get())));
    }
}
