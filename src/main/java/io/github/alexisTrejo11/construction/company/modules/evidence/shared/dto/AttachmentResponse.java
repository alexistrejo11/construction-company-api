package io.github.alexisTrejo11.construction.company.modules.evidence.shared.dto;

import java.time.Instant;

public record AttachmentResponse(
    Long id,
    Long evidenceId,
    String originalFileName,
    String contentType,
    long fileSize,
    String checksum,
    Instant createdAt
) {
}
