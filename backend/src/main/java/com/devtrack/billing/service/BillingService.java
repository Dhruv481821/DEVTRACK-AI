package com.devtrack.billing.service;

import com.devtrack.billing.dto.request.VerifyPaymentRequest;
import com.devtrack.billing.dto.response.BillingStatusResponse;
import com.devtrack.billing.dto.response.CreateOrderResponse;
import com.devtrack.billing.entity.BillingPlan;
import com.devtrack.billing.entity.PaymentOrder;
import com.devtrack.billing.entity.PaymentStatus;
import com.devtrack.billing.entity.ProEntitlement;
import com.devtrack.billing.repository.PaymentOrderRepository;
import com.devtrack.billing.repository.ProEntitlementRepository;
import com.devtrack.common.exception.ResourceNotFoundException;
import com.devtrack.common.exception.ValidationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Razorpay prepaid Pro passes.
 *
 * <p>Flow: createOrder (server picks the amount) → Razorpay Checkout in the browser → verifyPayment
 * with Razorpay's signature. The signed webhook is the safety net for payments whose browser closed
 * before verify ran. Both paths go through fulfil(), which locks the order row and is a no-op once
 * the order is PAID — so a payment extends Pro exactly once.
 */
@Service
public class BillingService {

  private static final Logger log = LoggerFactory.getLogger(BillingService.class);
  static final String CURRENCY = "INR";

  private final PaymentOrderRepository paymentOrderRepository;
  private final ProEntitlementRepository proEntitlementRepository;
  private final EntitlementService entitlementService;
  private final RazorpayClient razorpayClient;
  private final RazorpayProperties properties;
  private final ObjectMapper objectMapper;

  public BillingService(
      PaymentOrderRepository paymentOrderRepository,
      ProEntitlementRepository proEntitlementRepository,
      EntitlementService entitlementService,
      RazorpayClient razorpayClient,
      RazorpayProperties properties,
      ObjectMapper objectMapper) {
    this.paymentOrderRepository = paymentOrderRepository;
    this.proEntitlementRepository = proEntitlementRepository;
    this.entitlementService = entitlementService;
    this.razorpayClient = razorpayClient;
    this.properties = properties;
    this.objectMapper = objectMapper;
  }

  @Transactional(readOnly = true)
  public BillingStatusResponse status(UUID userId) {
    var proUntil = entitlementService.activeProUntil(userId);
    return new BillingStatusResponse(
        entitlementService.tierFor(userId),
        proUntil.orElse(null),
        properties.enabled(),
        properties.enabled() ? properties.keyId() : null,
        Arrays.stream(BillingPlan.values())
            .map(p -> new BillingStatusResponse.PlanOption(p, p.label(), p.amountPaise(), CURRENCY))
            .toList());
  }

  @Transactional
  public CreateOrderResponse createOrder(UUID userId, BillingPlan plan) {
    if (!properties.enabled()) {
      throw new ValidationException("Payments aren't available yet.");
    }
    // Receipt is our own reference (max 40 chars per Razorpay); notes help support look-ups.
    String receipt = "dt_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
    String razorpayOrderId =
        razorpayClient.createOrder(
            plan.amountPaise(),
            CURRENCY,
            receipt,
            Map.of("userId", userId.toString(), "plan", plan.name()));

    PaymentOrder order = new PaymentOrder();
    order.setUserId(userId);
    order.setRazorpayOrderId(razorpayOrderId);
    order.setPlan(plan);
    order.setAmountPaise(plan.amountPaise());
    order.setCurrency(CURRENCY);
    order.setCreatedAt(Instant.now());
    order.setUpdatedAt(Instant.now());
    paymentOrderRepository.save(order);

    return new CreateOrderResponse(
        razorpayOrderId, plan.amountPaise(), CURRENCY, properties.keyId(), plan, plan.label());
  }

  @Transactional
  public BillingStatusResponse verifyPayment(UUID userId, VerifyPaymentRequest request) {
    if (!properties.enabled()) {
      throw new ValidationException("Payments aren't available yet.");
    }
    PaymentOrder order =
        paymentOrderRepository
            .findByRazorpayOrderIdForUpdate(request.razorpayOrderId())
            // Another user's order is indistinguishable from a missing one (404-not-403).
            .filter(o -> o.getUserId().equals(userId))
            .orElseThrow(() -> new ResourceNotFoundException("Order not found."));

    if (!RazorpaySignatures.isValidPaymentSignature(
        request.razorpayOrderId(),
        request.razorpayPaymentId(),
        request.razorpaySignature(),
        properties.keySecret())) {
      log.warn("Rejected payment with invalid signature for order {}", order.getRazorpayOrderId());
      throw new ValidationException("Payment verification failed.");
    }

    fulfil(order, request.razorpayPaymentId());
    return status(userId);
  }

  /**
   * Returns false when the signature is invalid (controller answers 400 so Razorpay can surface
   * it); true otherwise, including for events we deliberately ignore.
   */
  @Transactional
  public boolean handleWebhook(String rawBody, String signature) {
    if (!properties.webhookEnabled()
        || !RazorpaySignatures.isValidWebhookSignature(
            rawBody, signature, properties.webhookSecret())) {
      log.warn("Rejected Razorpay webhook with missing/invalid signature");
      return false;
    }

    JsonNode root;
    try {
      root = objectMapper.readTree(rawBody);
    } catch (Exception e) {
      log.warn("Unparseable Razorpay webhook body", e);
      return true;
    }

    String event = root.path("event").asText("");
    if (!event.equals("payment.captured") && !event.equals("order.paid")) {
      return true;
    }
    JsonNode payment = root.path("payload").path("payment").path("entity");
    String orderId = payment.path("order_id").asText("");
    String paymentId = payment.path("id").asText("");
    long amount = payment.path("amount").asLong(-1);
    if (orderId.isEmpty() || paymentId.isEmpty()) {
      return true;
    }

    paymentOrderRepository
        .findByRazorpayOrderIdForUpdate(orderId)
        .ifPresentOrElse(
            order -> {
              if (amount != order.getAmountPaise()) {
                log.error(
                    "Webhook amount {} does not match order {} amount {}; not fulfilling",
                    amount,
                    orderId,
                    order.getAmountPaise());
                return;
              }
              fulfil(order, paymentId);
            },
            () -> log.warn("Webhook for unknown Razorpay order {}", orderId));
    return true;
  }

  /** Idempotent: an already-PAID order changes nothing. Caller holds the order's row lock. */
  void fulfil(PaymentOrder order, String razorpayPaymentId) {
    if (order.getStatus() == PaymentStatus.PAID) {
      return;
    }
    Instant now = Instant.now();
    order.setStatus(PaymentStatus.PAID);
    order.setRazorpayPaymentId(razorpayPaymentId);
    order.setPaidAt(now);
    order.setUpdatedAt(now);
    paymentOrderRepository.save(order);

    ProEntitlement entitlement =
        proEntitlementRepository
            .findById(order.getUserId())
            .orElseGet(
                () -> {
                  ProEntitlement created = new ProEntitlement();
                  created.setUserId(order.getUserId());
                  created.setProUntil(now);
                  return created;
                });
    // Stack passes: extend from the later of now and the current expiry.
    Instant base = entitlement.getProUntil().isAfter(now) ? entitlement.getProUntil() : now;
    entitlement.setProUntil(base.atZone(ZoneOffset.UTC).plus(order.getPlan().period()).toInstant());
    entitlement.setUpdatedAt(now);
    proEntitlementRepository.save(entitlement);

    log.info(
        "Fulfilled {} for user {} (order {}), Pro until {}",
        order.getPlan(),
        order.getUserId(),
        order.getRazorpayOrderId(),
        entitlement.getProUntil());
  }
}
