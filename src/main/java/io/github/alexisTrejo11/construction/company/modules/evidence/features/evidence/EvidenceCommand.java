package io.github.alexisTrejo11.construction.company.modules.evidence.features.evidence;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record EvidenceCommand(
    @NotBlank @Size(max = 150) String title,
    @Size(max = 5000) String description,
    @NotNull Instant recordedAt
) {
}
