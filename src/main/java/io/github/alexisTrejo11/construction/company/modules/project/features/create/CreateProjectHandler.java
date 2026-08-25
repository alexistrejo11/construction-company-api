package io.github.alexisTrejo11.construction.company.modules.project.features.create;

import io.github.alexisTrejo11.construction.company.modules.project.shared.persistence.ProjectRepository;
import io.github.alexisTrejo11.construction.company.modules.project.shared.domain.Project;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.GlobalAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import io.github.alexisTrejo11.construction.company.modules.project.members.shared.domain.ProjectMember;
import io.github.alexisTrejo11.construction.company.modules.project.members.shared.domain.ProjectMemberStatus;
import io.github.alexisTrejo11.construction.company.modules.project.members.shared.persistence.ProjectMemberRepository;
import io.github.alexisTrejo11.construction.company.modules.user.shared.persistence.UserRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateProjectHandler {
  private final ProjectRepository repository;
  private final CreateProjectMapper mapper;
  private final GlobalAuthorizationPolicy authorizationPolicy;
  private final ProjectMemberRepository projectMemberRepository;
  private final UserRepository userRepository;

  @Transactional
  public Result<CreateProjectResponse> execute(UserContext user, CreateProjectCommand request) {
    Result<Void> authorization = authorizationPolicy.requirePermission(user, Permission.PROJECT_CREATE);
    if (!authorization.isSuccess()) {
      return Result.forbidden(authorization.getErrorMessage());
    }

    if (repository.existsByCode(request.code())) {
      return Result.conflict("Code already exists");
    }

    Project entity = mapper.toEntity(request);

    Result<Void> validateResult = entity.validate();
    if (!validateResult.isSuccess()) {
      return Result.business(validateResult.getErrorMessage());
    }

    Project savedEntity = repository.save(entity);
    var creator = userRepository.findById(user.userId());
    if (creator.isEmpty()) {
      return Result.unauthenticated("Authenticated user was not found");
    }

    var membership = new ProjectMember();
    membership.setProject(savedEntity);
    membership.setUser(creator.get());
    membership.setStatus(ProjectMemberStatus.ACTIVE);
    membership.setAssignedAt(Instant.now());
    projectMemberRepository.save(membership);

    return Result.success(new CreateProjectResponse(savedEntity.getId()));
  }
}
