package com.devtrack.ai.dto.response;

/**
 * Quota-exceeded is deliberately not a value here — it's a 429 via AiQuotaExceededException before
 * a response body is ever built (see GlobalExceptionHandler). This enum only covers states where
 * the endpoint actually returns 200 with a body.
 */
public enum AtsScoreStatus {
  SUCCESS,
  CACHED_FALLBACK,
  UNAVAILABLE
}
