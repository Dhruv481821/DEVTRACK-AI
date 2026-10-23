package com.devtrack.ai.service;

import com.devtrack.billing.entity.Tier;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * The per-user daily AI quota key, shared by every agent so 09_AI_Architecture.md §7's cap is one
 * budget across all AI features.
 *
 * <p>Date-scoped (UTC): a fresh bucket each UTC day; GlobalExceptionHandler's Retry-After uses the
 * same boundary. Tier-scoped too, because a Bucket4j bucket keeps the limit it was created with —
 * without the tier in the key, a user who upgrades mid-day would stay on the free limit until
 * midnight.
 */
final class AiQuotaKeys {

  private static final String PREFIX = "ai-quota:";

  private AiQuotaKeys() {}

  static String dailyKey(UUID userId, Tier tier) {
    return PREFIX + userId + ":" + LocalDate.now(ZoneOffset.UTC) + ":" + tier.name();
  }
}
