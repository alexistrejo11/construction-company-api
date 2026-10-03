package io.github.alexisTrejo11.construction.company.modules.project.phases.features.update;

import io.github.alexisTrejo11.construction.company.modules.project.phases.features.create.CreateProjectPhaseCommand;
import io.github.alexisTrejo11.construction.company.modules.project.shared.dto.ProjectPhaseResponse;
import io.github.alexisTrejo11.construction.company.modules.project.shared.policy.ProjectAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import io.github.alexisTrejo11.construction.company.modules.project.phases.shared.domain.ProjectPhaseStatus;
import io.github.alexisTrejo11.construction.company.modules.project.phases.shared.persistence.ProjectPhaseRepository;
import io.github.alexisTrejo11.construction.company.modules.project.shared.mapper.ProjectResponseMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateProjectPhaseHandler {
  private final ProjectAuthorizationPolicy authorizationPolicy;
  private final ProjectPhaseRepository phaseRepository;
  private final ProjectResponseMapper mapper;

  @Transactional
  public Result<ProjectPhaseResponse> execute(
      UserContext user,
      Long projectId,
      Long phaseId,
      CreateProjectPhaseCommand command) {
    Result<Void> authorization = authorizationPolicy.requireProjectPermission(
        user,
        projectId,
        Permission.PHASE_UPDATE
    );
    if (!authorization.isSuccess()) {
      return Result.error(authorization.getErrorType(), authorization.getErrorMessage());
    }

    var phase = phaseRepository.findByIdAndProjectId(phaseId, projectId);
    if (phase.isEmpty()) {
      return Result.notFound("Project phase was not found");
    }
    if (phase.get().getStatus() == ProjectPhaseStatus.COMPLETED
        || phase.get().getStatus() == ProjectPhaseStatus.CANCELLED) {
      return Result.business("Terminal phases cannot be updated");
    }
    if (phaseRepository.existsByProjectIdAndSequenceOrderAndIdNot(
        projectId,
        command.sequenceOrder(),
        phaseId
    )) {
      return Result.conflict("Sequence order is already used in this project");
    }

    phase.get().setName(command.name());
    phase.get().setSequenceOrder(command.sequenceOrder());
    phase.get().setAllocatedAmount(command.allocatedBudget());
    phase.get().setStartDate(command.startDate());
    phase.get().setEndDate(command.endDate());
    Result<Void> validation = phase.get().validate();
    if (!validation.isSuccess()) {
      return Result.error(validation.getErrorType(), validation.getErrorMessage());
    }

    return Result.success(mapper.toPhaseResponse(phaseRepository.save(phase.get())));
  }
}
