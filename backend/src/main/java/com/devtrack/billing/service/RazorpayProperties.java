package com.devtrack.billing.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * devtrack.billing.razorpay.* — values come only from env vars (RAZORPAY_KEY_ID,
 * RAZORPAY_KEY_SECRET, RAZORPAY_WEBHOOK_SECRET). keyId is public (Checkout needs it in the
 * browser); the two secrets never leave the server.
 */
@ConfigurationProperties(prefix = "devtrack.billing.razorpay")
public record RazorpayProperties(String keyId, String keySecret, String webhookSecret) {

  public boolean enabled() {
    return keyId != null && !keyId.isBlank() && keySecret != null && !keySecret.isBlank();
  }

  public boolean webhookEnabled() {
    return webhookSecret != null && !webhookSecret.isBlank();
  }
}
