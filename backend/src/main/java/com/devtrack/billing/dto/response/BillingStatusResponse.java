package com.devtrack.billing.dto.response;

import com.devtrack.billing.entity.BillingPlan;
import com.devtrack.billing.entity.Tier;
import java.time.Instant;
import java.util.List;

/**
 * enabled=false means Razorpay keys aren't configured on this server (e.g. local dev without test
 * keys) — the UI shows plans but disables purchase. keyId is Razorpay's public key id.
 */
public record BillingStatusResponse(
    Tier tier, Instant proUntil, boolean enabled, String keyId, List<PlanOption> plans) {

  public record PlanOption(BillingPlan plan, String label, int amountPaise, String currency) {}
}
