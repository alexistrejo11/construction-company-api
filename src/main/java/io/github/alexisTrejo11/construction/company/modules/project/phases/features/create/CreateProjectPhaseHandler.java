package io.github.alexisTrejo11.construction.company.modules.project.phases.features.create;

import io.github.alexisTrejo11.construction.company.modules.project.shared.dto.ProjectPhaseResponse;
import io.github.alexisTrejo11.construction.company.modules.project.shared.policy.ProjectAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import io.github.alexisTrejo11.construction.company.modules.project.phases.shared.domain.ProjectPhase;
import io.github.alexisTrejo11.construction.company.modules.project.phases.shared.persistence.ProjectPhaseRepository;
import io.github.alexisTrejo11.construction.company.modules.project.shared.persistence.ProjectRepository;
import io.github.alexisTrejo11.construction.company.modules.project.shared.mapper.ProjectResponseMapper;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateProjectPhaseHandler {
  private final ProjectAuthorizationPolicy authorizationPolicy;
  private final ProjectRepository projectRepository;
  private final ProjectPhaseRepository phaseRepository;
  private final ProjectResponseMapper mapper;

  @Transactional
  public Result<ProjectPhaseResponse> execute(
      UserContext user,
      Long projectId,
      CreateProjectPhaseCommand command) {
    Result<Void> authorization = authorizationPolicy.requireProjectPermission(
        user,
        projectId,
        Permission.PHASE_CREATE
    );
    if (!authorization.isSuccess()) {
      return Result.error(authorization.getErrorType(), authorization.getErrorMessage());
    }

    var project = projectRepository.findById(projectId);
    if (project.isEmpty()) {
      return Result.notFound("Project not found");
    }
    if (phaseRepository.existsByProjectIdAndSequenceOrder(projectId, command.sequenceOrder())) {
      return Result.conflict("Sequence order is already used in this project");
    }

    var phase = new ProjectPhase();
    phase.setProject(project.get());
    phase.setName(command.name());
    phase.setSequenceOrder(command.sequenceOrder());
    phase.setAllocatedAmount(command.allocatedBudget());
    phase.setStartDate(command.startDate());
    phase.setEndDate(command.endDate());
    Result<Void> validation = phase.validate();
    if (!validation.isSuccess()) {
      return Result.error(validation.getErrorType(), validation.getErrorMessage());
    }

    return Result.success(mapper.toPhaseResponse(phaseRepository.save(phase)));
  }
}
