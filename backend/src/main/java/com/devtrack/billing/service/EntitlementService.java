package com.devtrack.billing.service;

import com.devtrack.billing.entity.ProEntitlement;
import com.devtrack.billing.entity.Tier;
import com.devtrack.billing.repository.ProEntitlementRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The billing module's public read API for other modules (the ai module asks "is this user Pro?"
 * here rather than touching billing's repositories).
 */
@Service
public class EntitlementService {

  private final ProEntitlementRepository proEntitlementRepository;

  public EntitlementService(ProEntitlementRepository proEntitlementRepository) {
    this.proEntitlementRepository = proEntitlementRepository;
  }

  @Transactional(readOnly = true)
  public Tier tierFor(UUID userId) {
    return activeProUntil(userId).isPresent() ? Tier.PRO : Tier.FREE;
  }

  /** The Pro expiry, only while it is still in the future. */
  @Transactional(readOnly = true)
  public Optional<Instant> activeProUntil(UUID userId) {
    Instant now = Instant.now();
    return proEntitlementRepository
        .findById(userId)
        .map(ProEntitlement::getProUntil)
        .filter(until -> until.isAfter(now));
  }
}
