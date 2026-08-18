package com.devtrack.common.exception;

/**
 * 429 — the per-user daily AI cap specifically. See 09_AI_Architecture.md §7. Not built yet — Phase
 * 5 — but the exception type is part of the shared hierarchy from day one.
 */
public class AiQuotaExceededException extends DevTrackException {
  public AiQuotaExceededException(String message) {
    super("AI_QUOTA_EXCEEDED", message);
  }
}
