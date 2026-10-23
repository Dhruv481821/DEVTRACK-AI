package com.devtrack.ai.dto.response;

/** Same states and meaning as AtsScoreStatus; quota exhaustion is a 429, not a value here. */
public enum CareerAssistantStatus {
  SUCCESS,
  CACHED_FALLBACK,
  UNAVAILABLE
}
