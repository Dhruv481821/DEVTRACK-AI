package com.devtrack.ai.dto.response;

import java.time.Instant;

/**
 * Always a 200 for the states this type represents — a genuine client error (bad resumeId, blank
 * jobDescription) is still a normal 4xx via GlobalExceptionHandler, never wrapped in this.
 * result/generatedAt are null for UNAVAILABLE; message is null for SUCCESS/CACHED_FALLBACK.
 */
public record AtsScoreResponse(
    AtsScoreStatus status, AtsScoreResult result, Instant generatedAt, String message) {

  public static AtsScoreResponse success(AtsScoreResult result, Instant generatedAt) {
    return new AtsScoreResponse(AtsScoreStatus.SUCCESS, result, generatedAt, null);
  }

  public static AtsScoreResponse cachedFallback(AtsScoreResult result, Instant generatedAt) {
    return new AtsScoreResponse(AtsScoreStatus.CACHED_FALLBACK, result, generatedAt, null);
  }

  public static AtsScoreResponse unavailable(String message) {
    return new AtsScoreResponse(AtsScoreStatus.UNAVAILABLE, null, null, message);
  }
}
