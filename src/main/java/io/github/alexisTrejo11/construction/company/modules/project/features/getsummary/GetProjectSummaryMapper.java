package io.github.alexisTrejo11.construction.company.modules.project.features.getsummary;

import io.github.alexisTrejo11.construction.company.modules.project.shared.domain.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface GetProjectSummaryMapper {

  @Mapping(target = "spentBudget", expression = "java(java.math.BigDecimal.ZERO)")
  @Mapping(target = "remainingBudget", source = "totalBudget")
  @Mapping(target = "completionPercentage", expression = "java(0.0)")
  @Mapping(target = "totalPhases", constant = "0")
  @Mapping(target = "totalMembers", constant = "0")
  GetProjectSummaryResponse toResponse(Project project);
}
