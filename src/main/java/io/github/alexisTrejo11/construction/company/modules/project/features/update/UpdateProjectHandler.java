package io.github.alexisTrejo11.construction.company.modules.project.features.update;

import io.github.alexisTrejo11.construction.company.modules.project.shared.dto.ProjectResponse;
import io.github.alexisTrejo11.construction.company.modules.project.shared.mapper.ProjectResponseMapper;
import io.github.alexisTrejo11.construction.company.modules.project.shared.persistence.ProjectRepository;
import io.github.alexisTrejo11.construction.company.modules.project.shared.domain.Project;
import io.github.alexisTrejo11.construction.company.shared.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateProjectHandler {
  private final ProjectRepository repository;
  private final ProjectResponseMapper mapper;

  @Transactional
  public Result<ProjectResponse> execute(Long projectId, UpdateProjectCommand command) {
    return repository.findById(projectId)
        .map(project -> apply(project, command))
        .orElseGet(() -> Result.notFound("Project not found"));
  }

  private Result<ProjectResponse> apply(Project project, UpdateProjectCommand command) {
    if (command.name() != null) {
      project.setName(command.name());
    }
    if (command.description() != null) {
      project.setDescription(command.description());
    }
    if (command.totalBudget() != null) {
      project.setTotalBudget(command.totalBudget());
    }
    if (command.location() != null) {
      project.setLocation(mapper.toSiteLocation(command.location()));
    }
    if (command.startDate() != null) {
      project.setStartDate(command.startDate());
    }
    if (command.estimatedEndDate() != null) {
      project.setEstimatedEndDate(command.estimatedEndDate());
    }
    if (command.actualEndDate() != null) {
      project.setActualEndDate(command.actualEndDate());
    }

    Result<Void> validateResult = project.validate();
    if (!validateResult.isSuccess()) {
      return Result.business(validateResult.getErrorMessage());
    }

    return Result.success(mapper.toResponse(repository.save(project)));
  }
}
