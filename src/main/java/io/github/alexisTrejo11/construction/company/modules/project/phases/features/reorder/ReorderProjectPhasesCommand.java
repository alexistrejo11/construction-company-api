package io.github.alexisTrejo11.construction.company.modules.project.phases.features.reorder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ReorderProjectPhasesCommand(
    @NotEmpty(message = "Phases list cannot be empty")
    @Valid
    List<PhaseOrder> phases
) {
  public record PhaseOrder(
      @NotNull(message = "Phase ID is required")
      Long phaseId,

      @NotNull(message = "Sequence order is required")
      @Min(value = 1, message = "Sequence order must be at least 1")
      Integer sequenceOrder
  ) {
  }
}
