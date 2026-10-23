package com.devtrack.ai.service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * The per-user daily AI quota key, shared by every agent so that 09_AI_Architecture.md §7's cap
 * (devtrack.ai.daily-request-limit) is one budget across all AI features, not one per agent.
 *
 * <p>Date-scoped (UTC) so a fresh bucket appears each UTC day — GlobalExceptionHandler's
 * Retry-After for AiQuotaExceededException is computed from the same UTC-midnight boundary.
 */
final class AiQuotaKeys {

  private static final String PREFIX = "ai-quota:";

  private AiQuotaKeys() {}

  static String dailyKey(UUID userId) {
    return PREFIX + userId + ":" + LocalDate.now(ZoneOffset.UTC);
  }
}
