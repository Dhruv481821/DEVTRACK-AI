package com.devtrack.ai.service;

import com.devtrack.ai.AiProperties;
import com.devtrack.ai.dto.response.AiQuotaResponse;
import com.devtrack.billing.entity.Tier;
import com.devtrack.billing.service.EntitlementService;
import com.devtrack.common.exception.AiQuotaExceededException;
import com.devtrack.common.security.RateLimiterService;
import java.util.UUID;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

/**
 * Single place that decides "may this user make one more AI request today?" — the limit depends on
 * the billing tier (EntitlementService, the billing module's public API). Every agent calls
 * consumeOrThrow before contacting the provider, and only on a cache miss.
 */
@Service
@EnableConfigurationProperties(AiProperties.class)
public class AiQuotaService {

  private final RateLimiterService rateLimiterService;
  private final EntitlementService entitlementService;
  private final AiProperties aiProperties;

  public AiQuotaService(
      RateLimiterService rateLimiterService,
      EntitlementService entitlementService,
      AiProperties aiProperties) {
    this.rateLimiterService = rateLimiterService;
    this.entitlementService = entitlementService;
    this.aiProperties = aiProperties;
  }

  public void consumeOrThrow(UUID userId) {
    Tier tier = entitlementService.tierFor(userId);
    int limit = limitFor(tier);
    if (!rateLimiterService.tryConsumeDaily(AiQuotaKeys.dailyKey(userId, tier), limit)) {
      throw new AiQuotaExceededException(
          tier == Tier.PRO
              ? "You've used today's " + limit + " AI requests. Try again tomorrow."
              : "You've used today's "
                  + limit
                  + " free AI requests. Upgrade to Pro for "
                  + aiProperties.proDailyRequestLimit()
                  + " a day.");
    }
  }

  public AiQuotaResponse describe(UUID userId) {
    Tier tier = entitlementService.tierFor(userId);
    return new AiQuotaResponse(
        tier,
        limitFor(tier),
        aiProperties.freeDailyRequestLimit(),
        aiProperties.proDailyRequestLimit());
  }

  private int limitFor(Tier tier) {
    return tier == Tier.PRO
        ? aiProperties.proDailyRequestLimit()
        : aiProperties.freeDailyRequestLimit();
  }
}
