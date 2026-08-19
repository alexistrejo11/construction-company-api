package io.github.alexisTrejo11.construction.company.shared;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public final class AppErrorResolver {
  private AppErrorResolver() {}
  public static ResponseEntity<ResponseWrapper<?>> handleResult(Result<?> result) {
    var type = result.getErrorType();
    var status = switch (type) {
      case VALIDATION -> HttpStatus.BAD_REQUEST;
      case UNAUTHENTICATED -> HttpStatus.UNAUTHORIZED;
      case FORBIDDEN -> HttpStatus.FORBIDDEN;
      case NOT_FOUND -> HttpStatus.NOT_FOUND;
      case CONFLICT -> HttpStatus.CONFLICT;
      case BUSINESS_RULE -> HttpStatus.UNPROCESSABLE_ENTITY;
      case UNKNOWN -> HttpStatus.INTERNAL_SERVER_ERROR;
    };
    return ResponseEntity.status(status).body(ResponseWrapper.failure(result.getErrorMessage(), type, type.name(), null));
  }
}
