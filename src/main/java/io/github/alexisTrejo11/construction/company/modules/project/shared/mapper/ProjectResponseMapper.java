package io.github.alexisTrejo11.construction.company.modules.project.shared.mapper;

import io.github.alexisTrejo11.construction.company.modules.project.shared.domain.SiteLocation;
import io.github.alexisTrejo11.construction.company.modules.project.shared.dto.ProjectMemberResponse;
import io.github.alexisTrejo11.construction.company.modules.project.shared.dto.ProjectPhaseResponse;
import io.github.alexisTrejo11.construction.company.modules.project.shared.dto.ProjectResponse;
import io.github.alexisTrejo11.construction.company.modules.project.shared.dto.SiteLocationRequest;
import io.github.alexisTrejo11.construction.company.modules.project.shared.dto.SiteLocationResponse;
import io.github.alexisTrejo11.construction.company.modules.project.members.shared.domain.ProjectMember;
import io.github.alexisTrejo11.construction.company.modules.project.phases.shared.domain.ProjectPhase;
import io.github.alexisTrejo11.construction.company.modules.project.shared.domain.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProjectResponseMapper {

  @Mapping(target = "phases", ignore = true)
  @Mapping(target = "members", ignore = true)
  ProjectResponse toResponse(Project project);

  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "role", ignore = true)
  ProjectMemberResponse toMemberResponse(ProjectMember member);

  @Mapping(target = "allocatedBudget", source = "allocatedAmount")
  ProjectPhaseResponse toPhaseResponse(ProjectPhase phase);

  SiteLocationResponse toSiteLocationResponse(SiteLocation siteLocation);

  SiteLocation toSiteLocation(SiteLocationRequest siteLocationRequest);

  default LocalDateTime map(Instant value) {
    return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
  }
}
