package io.github.alexisTrejo11.construction.company.modules.project.phases.features.status;

import io.github.alexisTrejo11.construction.company.modules.project.phases.shared.persistence.ProjectPhaseRepository;
import io.github.alexisTrejo11.construction.company.modules.project.shared.dto.ProjectPhaseResponse;
import io.github.alexisTrejo11.construction.company.modules.project.shared.mapper.ProjectResponseMapper;
import io.github.alexisTrejo11.construction.company.modules.project.shared.policy.ProjectAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateProjectPhaseStatusHandler {
    private final ProjectAuthorizationPolicy authorizationPolicy;
    private final ProjectPhaseRepository phaseRepository;
    private final ProjectResponseMapper mapper;

    @Transactional
    public Result<ProjectPhaseResponse> execute(
        UserContext actor,
        Long projectId,
        Long phaseId,
        UpdateProjectPhaseStatusCommand command
    ) {
        Result<Void> authorization = authorizationPolicy.requireProjectPermission(
            actor,
            projectId,
            Permission.PHASE_CHANGE_STATUS
        );
        if (!authorization.isSuccess()) {
            return Result.error(authorization.getErrorType(), authorization.getErrorMessage());
        }

        var phase = phaseRepository.findByIdAndProjectId(phaseId, projectId);
        if (phase.isEmpty()) {
            return Result.notFound("Project phase was not found");
        }

        Result<Void> transition = phase.get().updateStatus(command.status());
        if (!transition.isSuccess()) {
            return Result.error(transition.getErrorType(), transition.getErrorMessage());
        }

        return Result.success(mapper.toPhaseResponse(phaseRepository.save(phase.get())));
    }
}
