package com.devtrack.billing.controller;

import com.devtrack.billing.dto.request.CreateOrderRequest;
import com.devtrack.billing.dto.request.VerifyPaymentRequest;
import com.devtrack.billing.dto.response.BillingStatusResponse;
import com.devtrack.billing.dto.response.CreateOrderResponse;
import com.devtrack.billing.service.BillingService;
import com.devtrack.common.dto.ApiEnvelope;
import com.devtrack.common.security.CurrentUserResolver;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Authenticated billing endpoints. The webhook is separate (RazorpayWebhookController). */
@RestController
@RequestMapping("/api/v1/billing")
public class BillingController {

  private final BillingService billingService;
  private final CurrentUserResolver currentUserResolver;

  public BillingController(BillingService billingService, CurrentUserResolver currentUserResolver) {
    this.billingService = billingService;
    this.currentUserResolver = currentUserResolver;
  }

  @GetMapping("/status")
  public ApiEnvelope<BillingStatusResponse> status() {
    return ApiEnvelope.success(billingService.status(currentUserResolver.getCurrentUserId()));
  }

  @PostMapping("/orders")
  public ApiEnvelope<CreateOrderResponse> createOrder(
      @Valid @RequestBody CreateOrderRequest request) {
    return ApiEnvelope.success(
        billingService.createOrder(currentUserResolver.getCurrentUserId(), request.plan()));
  }

  @PostMapping("/orders/verify")
  public ApiEnvelope<BillingStatusResponse> verify(
      @Valid @RequestBody VerifyPaymentRequest request) {
    return ApiEnvelope.success(
        billingService.verifyPayment(currentUserResolver.getCurrentUserId(), request));
  }
}
