package com.devtrack.billing.service;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Razorpay's documented HMAC-SHA256 signatures, compared in constant time.
 *
 * <ul>
 *   <li>Checkout success: hex(HMAC(key_secret, order_id + "|" + payment_id))
 *   <li>Webhook: hex(HMAC(webhook_secret, raw request body))
 * </ul>
 */
public final class RazorpaySignatures {

  private RazorpaySignatures() {}

  public static boolean isValidPaymentSignature(
      String orderId, String paymentId, String signature, String keySecret) {
    return matches(hmacHex(orderId + "|" + paymentId, keySecret), signature);
  }

  public static boolean isValidWebhookSignature(
      String rawBody, String signature, String webhookSecret) {
    return matches(hmacHex(rawBody, webhookSecret), signature);
  }

  static String hmacHex(String payload, String secret) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new IllegalStateException("HMAC-SHA256 unavailable", e);
    }
  }

  private static boolean matches(String expectedHex, String providedHex) {
    if (providedHex == null) {
      return false;
    }
    return MessageDigest.isEqual(
        expectedHex.getBytes(StandardCharsets.UTF_8),
        providedHex.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
  }
}
