package io.github.alexisTrejo11.construction.company.modules.project.phases.features.reorder;

import org.springframework.stereotype.Service;
import io.github.alexisTrejo11.construction.company.modules.project.shared.policy.ProjectAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import io.github.alexisTrejo11.construction.company.modules.project.phases.shared.persistence.ProjectPhaseRepository;
import java.util.HashSet;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReorderProjectPhasesHandler {
  private final ProjectAuthorizationPolicy authorizationPolicy;
  private final ProjectPhaseRepository phaseRepository;

  @Transactional
  public Result<Void> execute(
      UserContext user,
      Long projectId,
      ReorderProjectPhasesCommand command) {
    Result<Void> authorization = authorizationPolicy.requireProjectPermission(
        user,
        projectId,
        Permission.PHASE_REORDER
    );
    if (!authorization.isSuccess()) {
      return Result.error(authorization.getErrorType(), authorization.getErrorMessage());
    }

    var phases = phaseRepository.findByProjectIdOrderBySequenceOrder(projectId);
    var byId = phases.stream().collect(java.util.stream.Collectors.toMap(
        phase -> phase.getId(),
        phase -> phase
    ));
    var sequenceOrders = new HashSet<Integer>();
    if (command.phases().size() != phases.size()) {
      return Result.validation("All project phases must be included in the reorder request");
    }

    for (var order : command.phases()) {
      var phase = byId.get(order.phaseId());
      if (phase == null || order.sequenceOrder() == null || order.sequenceOrder() < 1
          || !sequenceOrders.add(order.sequenceOrder())) {
        return Result.validation("Phase IDs and sequence orders must belong to this project and be unique");
      }
      phase.setSequenceOrder(order.sequenceOrder());
    }

    phaseRepository.saveAll(phases);
    return Result.success();
  }
}
