package io.github.alexisTrejo11.construction.company.shared;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(String error_type, String code, String message, Object details) {}
