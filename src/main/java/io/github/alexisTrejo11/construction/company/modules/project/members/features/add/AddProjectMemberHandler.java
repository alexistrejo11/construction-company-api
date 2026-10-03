package io.github.alexisTrejo11.construction.company.modules.project.members.features.add;

import io.github.alexisTrejo11.construction.company.modules.project.members.shared.domain.ProjectMember;
import io.github.alexisTrejo11.construction.company.modules.project.members.shared.domain.ProjectMemberStatus;
import io.github.alexisTrejo11.construction.company.modules.project.members.shared.persistence.ProjectMemberRepository;
import io.github.alexisTrejo11.construction.company.modules.project.shared.dto.ProjectMemberResponse;
import io.github.alexisTrejo11.construction.company.modules.project.shared.mapper.ProjectResponseMapper;
import io.github.alexisTrejo11.construction.company.modules.project.shared.persistence.ProjectRepository;
import io.github.alexisTrejo11.construction.company.modules.project.shared.policy.ProjectAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.modules.user.shared.persistence.UserRepository;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddProjectMemberHandler {
    private final ProjectAuthorizationPolicy authorizationPolicy;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository memberRepository;
    private final ProjectResponseMapper mapper;

    @Transactional
    public Result<ProjectMemberResponse> execute(
        UserContext actor,
        Long projectId,
        AddProjectMemberCommand command
    ) {
        Result<Void> authorization = authorizationPolicy.requireProjectPermission(
            actor,
            projectId,
            Permission.MEMBER_MANAGE
        );
        if (!authorization.isSuccess()) {
            return Result.error(authorization.getErrorType(), authorization.getErrorMessage());
        }

        var project = projectRepository.findById(projectId);
        if (project.isEmpty()) {
            return Result.notFound("Project not found");
        }

        var user = userRepository.findById(command.userId());
        if (user.isEmpty()) {
            return Result.notFound("User was not found");
        }

        var existing = memberRepository.findByProjectIdAndUserId(projectId, command.userId());
        if (existing.isPresent()) {
            if (existing.get().getStatus() == ProjectMemberStatus.ACTIVE) {
                return Result.conflict("User is already an active project member");
            }

            existing.get().setStatus(ProjectMemberStatus.ACTIVE);
            existing.get().setDeactivatedAt(null);
            return Result.success(mapper.toMemberResponse(memberRepository.save(existing.get())));
        }

        var member = new ProjectMember();
        member.setProject(project.get());
        member.setUser(user.get());
        member.setStatus(ProjectMemberStatus.ACTIVE);
        member.setAssignedAt(Instant.now());
        return Result.success(mapper.toMemberResponse(memberRepository.save(member)));
    }
}
