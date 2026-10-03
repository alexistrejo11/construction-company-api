package io.github.alexisTrejo11.construction.company.shared;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import org.slf4j.MDC;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResponseWrapper<T>(String message, T data, ApiError error, Instant timestamp, String traceId) {
  public static <T> ResponseWrapper<T> success(T data, String message) { return new ResponseWrapper<>(message, data, null, Instant.now(), MDC.get("traceId")); }
  public static <T> ResponseWrapper<T> created(T data, String entity) { return success(data, entity + " created successfully"); }
  public static <T> ResponseWrapper<T> found(T data, String entity) { return success(data, entity + " fetched successfully"); }
  public static <T> ResponseWrapper<T> found(T data, String entity, Object parameter, Object value) { return found(data, entity); }
  public static <T> ResponseWrapper<T> success(String message) { return success(null, message); }
  public static <T> ResponseWrapper<T> deleted(String entity) { return success(entity + " deleted successfully"); }
  public static <T> ResponseWrapper<T> failure(String message, Result.ErrorType type, String code, Object details) { return new ResponseWrapper<>(message, null, new ApiError(type.name(), code, message, details), Instant.now(), MDC.get("traceId")); }
}
