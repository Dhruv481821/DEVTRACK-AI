package com.devtrack.common.exception;

import com.devtrack.common.dto.ApiEnvelope;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * The one place HTTP status codes get decided — see /docs/07_Backend_Architecture.md §4 and
 * /docs/06_API_Specification.md §1.4. No controller hand-rolls a status code.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiEnvelope<Void>> handleNotFound(ResourceNotFoundException e) {
    return respond(HttpStatus.NOT_FOUND, e.getErrorCode(), e.getMessage(), null);
  }

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ApiEnvelope<Void>> handleValidation(ValidationException e) {
    return respond(HttpStatus.BAD_REQUEST, e.getErrorCode(), e.getMessage(), null);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiEnvelope<Void>> handleAuth(AuthenticationException e) {
    return respond(HttpStatus.UNAUTHORIZED, e.getErrorCode(), e.getMessage(), null);
  }

  @ExceptionHandler(AuthorizationException.class)
  public ResponseEntity<ApiEnvelope<Void>> handleAuthz(AuthorizationException e) {
    return respond(HttpStatus.FORBIDDEN, e.getErrorCode(), e.getMessage(), null);
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ApiEnvelope<Void>> handleConflict(ConflictException e) {
    return respond(HttpStatus.CONFLICT, e.getErrorCode(), e.getMessage(), null);
  }

  @ExceptionHandler({RateLimitExceededException.class, AiQuotaExceededException.class})
  public ResponseEntity<ApiEnvelope<Void>> handleRateLimit(DevTrackException e) {
    return respond(HttpStatus.TOO_MANY_REQUESTS, e.getErrorCode(), e.getMessage(), null);
  }

  /** Bean Validation failures on @Valid request DTOs — translated into the same envelope shape. */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiEnvelope<Void>> handleBeanValidation(MethodArgumentNotValidException e) {
    List<ApiEnvelope.FieldError> details =
        e.getBindingResult().getFieldErrors().stream()
            .map(
                (FieldError fe) ->
                    new ApiEnvelope.FieldError(
                        fe.getField(),
                        fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid"))
            .toList();
    return respond(
        HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed.", details);
  }

  /**
   * Catch-all for anything not in the hierarchy above — a genuine bug, not an expected business
   * condition. Never leaks a stack trace or internal detail to the client (06_API_Specification.md
   * §1.4) — logged with a correlation ID instead, per 07_Backend_Architecture.md §7 (wired up when
   * the correlation-ID filter is added in the next slice).
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiEnvelope<Void>> handleUnexpected(Exception e) {
    String correlationId = MDC.get("correlationId");
    if (correlationId == null) {
      correlationId = UUID.randomUUID().toString();
    }
    log.error("Unhandled exception [correlationId={}]", correlationId, e);
    return respond(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "INTERNAL_ERROR",
        "Something went wrong on our end. Reference: " + correlationId,
        null);
  }

  private ResponseEntity<ApiEnvelope<Void>> respond(
      HttpStatus status, String code, String message, List<ApiEnvelope.FieldError> details) {
    return ResponseEntity.status(status).body(ApiEnvelope.error(code, message, details));
  }
}
