package com.devtrack.billing.dto.request;

import com.devtrack.billing.entity.BillingPlan;
import jakarta.validation.constraints.NotNull;

/** Only the plan name — the amount is always taken from BillingPlan on the server. */
public record CreateOrderRequest(@NotNull(message = "Choose a plan") BillingPlan plan) {}
