package io.github.alexisTrejo11.construction.company.modules.project.phases.features.get;

import io.github.alexisTrejo11.construction.company.modules.project.shared.dto.ProjectPhaseResponse;
import io.github.alexisTrejo11.construction.company.modules.project.phases.shared.persistence.ProjectPhaseRepository;
import io.github.alexisTrejo11.construction.company.modules.project.shared.mapper.ProjectResponseMapper;
import io.github.alexisTrejo11.construction.company.modules.project.shared.policy.ProjectAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetProjectPhasesHandler {
  private final ProjectAuthorizationPolicy authorizationPolicy;
  private final ProjectPhaseRepository phaseRepository;
  private final ProjectResponseMapper mapper;

  public Result<List<ProjectPhaseResponse>> execute(
      UserContext user,
      GetProjectPhasesQuery query) {
    Result<Void> authorization = authorizationPolicy.requireProjectPermission(
        user,
        query.projectId(),
        Permission.PHASE_READ
    );
    if (!authorization.isSuccess()) {
      return Result.error(authorization.getErrorType(), authorization.getErrorMessage());
    }

    return Result.success(phaseRepository.findByProjectIdOrderBySequenceOrder(query.projectId())
        .stream()
        .map(mapper::toPhaseResponse)
        .toList());
  }
}
