package io.github.alexisTrejo11.construction.company.shared.pagination;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PageRequest(@Min(1) Integer page, @Min(1) @Max(100) Integer size, String sort) {
  public int pageOrDefault() { return page == null ? 1 : page; }
  public int sizeOrDefault() { return size == null ? 20 : size; }
}
