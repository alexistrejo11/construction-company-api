package io.github.alexisTrejo11.construction.company.config;

import io.github.alexisTrejo11.construction.company.shared.ResponseWrapper;
import io.github.alexisTrejo11.construction.company.shared.Result;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ResponseWrapper<?>> validation(MethodArgumentNotValidException exception) {
    var details = exception.getBindingResult().getFieldErrors().stream().map(this::field).toList();
    return ResponseEntity.badRequest().body(ResponseWrapper.failure("Request validation failed", Result.ErrorType.VALIDATION, "VALIDATION_FAILED", details));
  }
  @ExceptionHandler(EntityNotFoundException.class)
  ResponseEntity<ResponseWrapper<?>> notFound(EntityNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseWrapper.failure("Resource was not found", Result.ErrorType.NOT_FOUND, "RESOURCE_NOT_FOUND", null));
  }
  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<ResponseWrapper<?>> unreadableRequest(HttpMessageNotReadableException exception) {
    return ResponseEntity.badRequest().body(ResponseWrapper.failure("Request validation failed", Result.ErrorType.VALIDATION, "MALFORMED_REQUEST", null));
  }
  @ExceptionHandler(Exception.class)
  ResponseEntity<ResponseWrapper<?>> unexpected(Exception exception) {
    log.error("Unhandled request failure", exception);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseWrapper.failure("The request could not be processed", Result.ErrorType.UNKNOWN, "INTERNAL_ERROR", null));
  }
  private ValidationDetail field(FieldError error) { return new ValidationDetail(error.getField(), error.getDefaultMessage()); }
  private record ValidationDetail(String field, String message) {}
}
