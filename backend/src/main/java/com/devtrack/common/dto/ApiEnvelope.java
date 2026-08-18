package com.devtrack.common.dto;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;

/** Matches /docs/06_API_Specification.md §1.3 exactly — every response uses this shape. */
public record ApiEnvelope<T>(boolean success, T data, ErrorBody error, Meta meta) {

  public static <T> ApiEnvelope<T> success(T data) {
    return new ApiEnvelope<>(true, data, null, Meta.now(null));
  }

  /**
   * Deliberately NOT an overload of success() — a second success(Page<T>) method made every
   * existing success(null) call site elsewhere in the codebase ambiguous to the compiler (it
   * couldn't tell whether `null` was meant to resolve T against this overload or the plain one),
   * breaking AuthController and NotificationController's void-returning endpoints that were already
   * correct. A distinct name removes the ambiguity entirely instead of requiring every call site to
   * be rewritten with an explicit type witness.
   */
  public static <T> ApiEnvelope<List<T>> paginated(Page<T> page) {
    Pagination pagination =
        new Pagination(
            page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    return new ApiEnvelope<>(true, page.getContent(), null, Meta.now(pagination));
  }

  public static ApiEnvelope<Void> error(String code, String message, List<FieldError> details) {
    return new ApiEnvelope<>(false, null, new ErrorBody(code, message, details), Meta.now(null));
  }

  public record ErrorBody(String code, String message, List<FieldError> details) {}

  public record FieldError(String field, String reason) {}

  public record Pagination(int page, int size, long totalElements, int totalPages) {}

  public record Meta(Instant timestamp, Pagination pagination) {
    static Meta now(Pagination pagination) {
      return new Meta(Instant.now(), pagination);
    }
  }
}
