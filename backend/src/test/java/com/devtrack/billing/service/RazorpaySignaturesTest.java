package com.devtrack.billing.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Expected digests were computed independently (Python hmac/hashlib), not by this code. */
class RazorpaySignaturesTest {

  @Test
  void paymentSignature_matchesKnownVector() {
    assertThat(
            RazorpaySignatures.isValidPaymentSignature(
                "order_ABC",
                "pay_XYZ",
                "15656b40fea6f2159b578efa459e969de9f5e223fb8a08393e274ac578d9d005",
                "test_secret"))
        .isTrue();
  }

  @Test
  void paymentSignature_rejectsTamperedIdsWrongSecretAndNull() {
    String sig = "15656b40fea6f2159b578efa459e969de9f5e223fb8a08393e274ac578d9d005";
    assertThat(
            RazorpaySignatures.isValidPaymentSignature(
                "order_ABC", "pay_OTHER", sig, "test_secret"))
        .isFalse();
    assertThat(RazorpaySignatures.isValidPaymentSignature("order_ABC", "pay_XYZ", sig, "wrong"))
        .isFalse();
    assertThat(
            RazorpaySignatures.isValidPaymentSignature("order_ABC", "pay_XYZ", null, "test_secret"))
        .isFalse();
  }

  @Test
  void webhookSignature_isOverTheExactRawBody() {
    String sig = "ee5c85cadcdc92c9476aebf71c93808edffd3ff9888302227b46b9ae0dc729b2";
    assertThat(RazorpaySignatures.isValidWebhookSignature("{\"event\":\"ping\"}", sig, "wh_secret"))
        .isTrue();
    // Same JSON, different whitespace: must fail — the HMAC covers raw bytes.
    assertThat(
            RazorpaySignatures.isValidWebhookSignature("{\"event\": \"ping\"}", sig, "wh_secret"))
        .isFalse();
  }
}
