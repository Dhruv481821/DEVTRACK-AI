package com.devtrack.billing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.devtrack.billing.entity.ProEntitlement;
import com.devtrack.billing.entity.Tier;
import com.devtrack.billing.repository.ProEntitlementRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EntitlementServiceTest {

  private final ProEntitlementRepository repository = mock(ProEntitlementRepository.class);
  private final EntitlementService service = new EntitlementService(repository);
  private final UUID userId = UUID.randomUUID();

  @Test
  void neverBought_isFree() {
    when(repository.findById(userId)).thenReturn(Optional.empty());
    assertThat(service.tierFor(userId)).isEqualTo(Tier.FREE);
  }

  @Test
  void unexpiredPass_isPro() {
    when(repository.findById(userId)).thenReturn(Optional.of(entitlement(Duration.ofDays(3))));
    assertThat(service.tierFor(userId)).isEqualTo(Tier.PRO);
    assertThat(service.activeProUntil(userId)).isPresent();
  }

  @Test
  void expiredPass_fallsBackToFree() {
    when(repository.findById(userId)).thenReturn(Optional.of(entitlement(Duration.ofDays(-1))));
    assertThat(service.tierFor(userId)).isEqualTo(Tier.FREE);
    assertThat(service.activeProUntil(userId)).isEmpty();
  }

  private ProEntitlement entitlement(Duration fromNow) {
    ProEntitlement e = new ProEntitlement();
    e.setUserId(userId);
    e.setProUntil(Instant.now().plus(fromNow));
    return e;
  }
}
