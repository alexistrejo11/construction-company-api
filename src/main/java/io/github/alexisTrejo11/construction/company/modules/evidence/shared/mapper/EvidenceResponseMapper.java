package io.github.alexisTrejo11.construction.company.modules.evidence.shared.mapper;

import io.github.alexisTrejo11.construction.company.modules.evidence.shared.domain.Attachment;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.domain.Evidence;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.dto.AttachmentResponse;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.dto.EvidenceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EvidenceResponseMapper {
    @Mapping(target = "phaseId", source = "phase.id")
    @Mapping(target = "expenseId", source = "expense.id")
    @Mapping(target = "authorUserId", source = "author.id")
    EvidenceResponse toResponse(Evidence evidence);

    @Mapping(target = "evidenceId", source = "evidence.id")
    AttachmentResponse toResponse(Attachment attachment);
}
