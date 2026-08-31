package io.github.alexisTrejo11.construction.company.modules.evidence.shared.dto;

import java.time.Instant;

public record EvidenceResponse(
    Long id,
    Long phaseId,
    Long expenseId,
    Long authorUserId,
    String title,
    String description,
    Instant recordedAt,
    Instant createdAt,
    Instant updatedAt
) {
}
