package io.github.alexisTrejo11.construction.company.shared;

import lombok.Getter;

@Getter
public final class Result<T> {
  public enum ErrorType { VALIDATION, UNAUTHENTICATED, FORBIDDEN, NOT_FOUND, CONFLICT, BUSINESS_RULE, UNKNOWN }
  private final T data;
  private final String errorMessage;
  private final ErrorType errorType;
  private Result(T data, String errorMessage, ErrorType errorType) { this.data = data; this.errorMessage = errorMessage; this.errorType = errorType; }
  public boolean isSuccess() { return errorType == null; }
  public static <T> Result<T> success(T data) { return new Result<>(data, null, null); }
  public static Result<Void> success() { return success(null); }
  public static <T> Result<T> error(String message) { return failure(ErrorType.UNKNOWN, message); }
  public static <T> Result<T> error(ErrorType type, String message) { return failure(type, message); }
  public static <T> Result<T> validation(String message) { return failure(ErrorType.VALIDATION, message); }
  public static <T> Result<T> unauthenticated(String message) { return failure(ErrorType.UNAUTHENTICATED, message); }
  public static <T> Result<T> forbidden(String message) { return failure(ErrorType.FORBIDDEN, message); }
  public static <T> Result<T> notFound(String message) { return failure(ErrorType.NOT_FOUND, message); }
  public static <T> Result<T> conflict(String message) { return failure(ErrorType.CONFLICT, message); }
  public static <T> Result<T> business(String message) { return failure(ErrorType.BUSINESS_RULE, message); }
  private static <T> Result<T> failure(ErrorType type, String message) { return new Result<>(null, message, type); }
}
