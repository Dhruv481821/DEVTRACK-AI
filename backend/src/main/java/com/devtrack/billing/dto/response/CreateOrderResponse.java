package com.devtrack.billing.dto.response;

import com.devtrack.billing.entity.BillingPlan;

/** Everything Razorpay Checkout needs to open for this order. */
public record CreateOrderResponse(
    String orderId,
    int amountPaise,
    String currency,
    String keyId,
    BillingPlan plan,
    String label) {}
