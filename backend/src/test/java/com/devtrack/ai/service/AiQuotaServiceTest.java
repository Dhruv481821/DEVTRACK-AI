package com.devtrack.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.devtrack.ai.AiProperties;
import com.devtrack.billing.entity.Tier;
import com.devtrack.billing.service.EntitlementService;
import com.devtrack.common.exception.AiQuotaExceededException;
import com.devtrack.common.security.RateLimiterService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AiQuotaServiceTest {

  private RateLimiterService rateLimiterService;
  private EntitlementService entitlementService;
  private AiQuotaService service;
  private final UUID userId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    rateLimiterService = mock(RateLimiterService.class);
    entitlementService = mock(EntitlementService.class);
    service = new AiQuotaService(rateLimiterService, entitlementService, new AiProperties(3, 30));
  }

  @Test
  void freeUser_usesFreeLimitAndTierScopedKey() {
    when(entitlementService.tierFor(userId)).thenReturn(Tier.FREE);
    when(rateLimiterService.tryConsumeDaily(endsWith(":FREE"), eq(3))).thenReturn(false);

    assertThatThrownBy(() -> service.consumeOrThrow(userId))
        .isInstanceOf(AiQuotaExceededException.class)
        .hasMessageContaining("Upgrade to Pro for 30 a day");
  }

  @Test
  void proUser_usesProLimitInItsOwnBucket() {
    when(entitlementService.tierFor(userId)).thenReturn(Tier.PRO);
    when(rateLimiterService.tryConsumeDaily(endsWith(":PRO"), eq(30))).thenReturn(true);

    service.consumeOrThrow(userId);

    assertThat(service.describe(userId).dailyLimit()).isEqualTo(30);
  }

  @Test
  void proUserOverLimit_isNotToldToUpgrade() {
    when(entitlementService.tierFor(userId)).thenReturn(Tier.PRO);
    when(rateLimiterService.tryConsumeDaily(endsWith(":PRO"), eq(30))).thenReturn(false);

    assertThatThrownBy(() -> service.consumeOrThrow(userId))
        .isInstanceOf(AiQuotaExceededException.class)
        .hasMessageNotContaining("Upgrade");
  }
}
