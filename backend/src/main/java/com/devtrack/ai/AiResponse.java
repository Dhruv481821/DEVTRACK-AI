package com.devtrack.ai;

/**
 * Result of a single AiProvider.generate() call. A provider never throws for a normal failure (rate
 * limited, malformed response, network error) — it reports success=false and lets the caller (an
 * agent service) decide what that failure means for its own degraded-mode UX, per
 * /docs/09_AI_Architecture.md §8. A provider only throws for genuine programmer errors (e.g. a null
 * request).
 */
public record AiResponse(boolean success, String jsonPayload, String errorMessage) {

  public static AiResponse success(String jsonPayload) {
    return new AiResponse(true, jsonPayload, null);
  }

  public static AiResponse failure(String errorMessage) {
    return new AiResponse(false, null, errorMessage);
  }
}
