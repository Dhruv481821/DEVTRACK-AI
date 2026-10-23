package com.devtrack.billing.controller;

import com.devtrack.billing.service.BillingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * Razorpay → server callback. Unauthenticated by necessity (SecurityConfig permits exactly this
 * POST); authenticity comes from the X-Razorpay-Signature HMAC over the RAW body, which is why the
 * body is bound as a String and not deserialised first.
 */
@RestController
public class RazorpayWebhookController {

  private final BillingService billingService;

  public RazorpayWebhookController(BillingService billingService) {
    this.billingService = billingService;
  }

  @PostMapping("/api/v1/billing/webhook")
  public ResponseEntity<Void> handle(
      @RequestBody String rawBody,
      @RequestHeader(name = "X-Razorpay-Signature", required = false) String signature) {
    return billingService.handleWebhook(rawBody, signature)
        ? ResponseEntity.ok().build()
        : ResponseEntity.badRequest().build();
  }
}
