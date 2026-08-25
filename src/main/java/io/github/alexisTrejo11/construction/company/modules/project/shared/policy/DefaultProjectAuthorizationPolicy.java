package io.github.alexisTrejo11.construction.company.modules.project.shared.policy;

import io.github.alexisTrejo11.construction.company.modules.project.members.shared.domain.ProjectMemberStatus;
import io.github.alexisTrejo11.construction.company.modules.project.members.shared.persistence.ProjectMemberRepository;
import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.UserRole;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import java.util.EnumSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultProjectAuthorizationPolicy implements ProjectAuthorizationPolicy {
    private static final Set<Permission> ADMIN_BYPASS_PERMISSIONS = EnumSet.of(
        Permission.PROJECT_CREATE,
        Permission.PROJECT_READ,
        Permission.PROJECT_UPDATE,
        Permission.PROJECT_CHANGE_STATUS,
        Permission.PHASE_CREATE,
        Permission.PHASE_READ,
        Permission.PHASE_UPDATE,
        Permission.PHASE_REORDER,
        Permission.MEMBER_MANAGE,
        Permission.MEMBER_READ
    );

    private final ProjectMemberRepository projectMemberRepository;

    @Override
    public Result<Void> requireProjectPermission(
        UserContext user,
        Long projectId,
        Permission permission
    ) {
        if (user == null || user.userId() == null) {
            return Result.unauthenticated("Authenticated user was not found");
        }

        if (!user.permissions().contains(permission)) {
            return Result.forbidden("You do not have permission to perform this operation");
        }

        if (user.roles().contains(UserRole.COMPANY_ADMIN)
            && ADMIN_BYPASS_PERMISSIONS.contains(permission)) {
            return Result.success();
        }

        if (projectMemberRepository.existsByProjectIdAndUserIdAndStatus(
            projectId,
            user.userId(),
            ProjectMemberStatus.ACTIVE
        )) {
            return Result.success();
        }

        return Result.forbidden("You must be an active member of this project");
    }
}
