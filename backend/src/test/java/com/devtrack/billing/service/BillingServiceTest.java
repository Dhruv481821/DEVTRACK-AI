package com.devtrack.billing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.devtrack.billing.dto.request.VerifyPaymentRequest;
import com.devtrack.billing.entity.BillingPlan;
import com.devtrack.billing.entity.PaymentOrder;
import com.devtrack.billing.entity.PaymentStatus;
import com.devtrack.billing.entity.ProEntitlement;
import com.devtrack.billing.repository.PaymentOrderRepository;
import com.devtrack.billing.repository.ProEntitlementRepository;
import com.devtrack.common.exception.ResourceNotFoundException;
import com.devtrack.common.exception.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class BillingServiceTest {

  private static final String KEY_SECRET = "test_secret";
  private static final String WEBHOOK_SECRET = "wh_secret";

  private PaymentOrderRepository orderRepository;
  private ProEntitlementRepository entitlementRepository;
  private RazorpayClient razorpayClient;
  private BillingService service;

  private final UUID userId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    orderRepository = mock(PaymentOrderRepository.class);
    entitlementRepository = mock(ProEntitlementRepository.class);
    razorpayClient = mock(RazorpayClient.class);
    service = serviceWith(new RazorpayProperties("rzp_test_key", KEY_SECRET, WEBHOOK_SECRET));
    when(entitlementRepository.findById(any())).thenReturn(Optional.empty());
  }

  private BillingService serviceWith(RazorpayProperties properties) {
    return new BillingService(
        orderRepository,
        entitlementRepository,
        new EntitlementService(entitlementRepository),
        razorpayClient,
        properties,
        new ObjectMapper());
  }

  @Test
  void createOrder_whenKeysMissing_isRefusedWithoutCallingRazorpay() {
    BillingService disabled = serviceWith(new RazorpayProperties("", "", ""));

    assertThatThrownBy(() -> disabled.createOrder(userId, BillingPlan.PRO_MONTHLY))
        .isInstanceOf(ValidationException.class);
    verifyNoInteractions(razorpayClient);
  }

  @Test
  void createOrder_usesTheServerSidePriceAndStoresTheOrder() {
    when(razorpayClient.createOrder(eq(199_900), eq("INR"), anyString(), anyMap()))
        .thenReturn("order_ABC");

    var response = service.createOrder(userId, BillingPlan.PRO_YEARLY);

    assertThat(response.orderId()).isEqualTo("order_ABC");
    assertThat(response.amountPaise()).isEqualTo(199_900);
    assertThat(response.keyId()).isEqualTo("rzp_test_key");
    ArgumentCaptor<PaymentOrder> saved = ArgumentCaptor.forClass(PaymentOrder.class);
    verify(orderRepository).save(saved.capture());
    assertThat(saved.getValue().getUserId()).isEqualTo(userId);
    assertThat(saved.getValue().getStatus()).isEqualTo(PaymentStatus.CREATED);
  }

  @Test
  void verifyPayment_withValidSignature_marksPaidAndGrantsOneMonth() {
    PaymentOrder order = order(userId, BillingPlan.PRO_MONTHLY);
    when(orderRepository.findByRazorpayOrderIdForUpdate("order_ABC"))
        .thenReturn(Optional.of(order));

    service.verifyPayment(userId, validVerifyRequest());

    assertThat(order.getStatus()).isEqualTo(PaymentStatus.PAID);
    assertThat(order.getRazorpayPaymentId()).isEqualTo("pay_XYZ");
    ArgumentCaptor<ProEntitlement> granted = ArgumentCaptor.forClass(ProEntitlement.class);
    verify(entitlementRepository).save(granted.capture());
    Instant expected = Instant.now().atZone(ZoneOffset.UTC).plusMonths(1).toInstant();
    assertThat(Duration.between(granted.getValue().getProUntil(), expected).abs())
        .isLessThan(Duration.ofMinutes(1));
  }

  @Test
  void verifyPayment_onAlreadyPaidOrder_doesNotExtendAgain() {
    PaymentOrder order = order(userId, BillingPlan.PRO_MONTHLY);
    order.setStatus(PaymentStatus.PAID);
    when(orderRepository.findByRazorpayOrderIdForUpdate("order_ABC"))
        .thenReturn(Optional.of(order));

    service.verifyPayment(userId, validVerifyRequest());

    verify(entitlementRepository, never()).save(any());
  }

  @Test
  void verifyPayment_withInvalidSignature_isRejectedAndNotFulfilled() {
    PaymentOrder order = order(userId, BillingPlan.PRO_MONTHLY);
    when(orderRepository.findByRazorpayOrderIdForUpdate("order_ABC"))
        .thenReturn(Optional.of(order));

    assertThatThrownBy(
            () ->
                service.verifyPayment(
                    userId, new VerifyPaymentRequest("order_ABC", "pay_XYZ", "deadbeef")))
        .isInstanceOf(ValidationException.class);
    assertThat(order.getStatus()).isEqualTo(PaymentStatus.CREATED);
    verify(entitlementRepository, never()).save(any());
  }

  @Test
  void verifyPayment_forAnotherUsersOrder_isNotFound() {
    PaymentOrder order = order(UUID.randomUUID(), BillingPlan.PRO_MONTHLY);
    when(orderRepository.findByRazorpayOrderIdForUpdate("order_ABC"))
        .thenReturn(Optional.of(order));

    assertThatThrownBy(() -> service.verifyPayment(userId, validVerifyRequest()))
        .isInstanceOf(ResourceNotFoundException.class);
    assertThat(order.getStatus()).isEqualTo(PaymentStatus.CREATED);
  }

  @Test
  void fulfil_stacksOnTopOfAnUnexpiredPass() {
    Instant currentUntil = Instant.now().plus(Duration.ofDays(10));
    ProEntitlement existing = new ProEntitlement();
    existing.setUserId(userId);
    existing.setProUntil(currentUntil);
    when(entitlementRepository.findById(userId)).thenReturn(Optional.of(existing));

    service.fulfil(order(userId, BillingPlan.PRO_YEARLY), "pay_1");

    assertThat(existing.getProUntil())
        .isEqualTo(currentUntil.atZone(ZoneOffset.UTC).plusYears(1).toInstant());
  }

  @Test
  void webhook_withInvalidSignature_returnsFalseAndTouchesNothing() {
    assertThat(service.handleWebhook("{\"event\":\"payment.captured\"}", "bad")).isFalse();
    verifyNoInteractions(orderRepository);
  }

  @Test
  void webhook_paymentCaptured_fulfilsTheMatchingOrder() {
    PaymentOrder order = order(userId, BillingPlan.PRO_MONTHLY);
    when(orderRepository.findByRazorpayOrderIdForUpdate("order_ABC"))
        .thenReturn(Optional.of(order));
    String body = capturedEvent("order_ABC", "pay_XYZ", 19_900);

    boolean accepted =
        service.handleWebhook(body, RazorpaySignatures.hmacHex(body, WEBHOOK_SECRET));

    assertThat(accepted).isTrue();
    assertThat(order.getStatus()).isEqualTo(PaymentStatus.PAID);
  }

  @Test
  void webhook_withMismatchedAmount_doesNotFulfil() {
    PaymentOrder order = order(userId, BillingPlan.PRO_YEARLY);
    when(orderRepository.findByRazorpayOrderIdForUpdate("order_ABC"))
        .thenReturn(Optional.of(order));
    String body = capturedEvent("order_ABC", "pay_XYZ", 100);

    service.handleWebhook(body, RazorpaySignatures.hmacHex(body, WEBHOOK_SECRET));

    assertThat(order.getStatus()).isEqualTo(PaymentStatus.CREATED);
    verify(entitlementRepository, never()).save(any());
  }

  @Test
  void createOrder_neverAcceptsAClientAmount() {
    // There is no amount parameter at all: the only input is the plan enum.
    when(razorpayClient.createOrder(anyInt(), anyString(), anyString(), anyMap()))
        .thenReturn("order_1");
    assertThat(service.createOrder(userId, BillingPlan.PRO_MONTHLY).amountPaise())
        .isEqualTo(BillingPlan.PRO_MONTHLY.amountPaise());
  }

  private VerifyPaymentRequest validVerifyRequest() {
    return new VerifyPaymentRequest(
        "order_ABC", "pay_XYZ", RazorpaySignatures.hmacHex("order_ABC|pay_XYZ", KEY_SECRET));
  }

  private PaymentOrder order(UUID owner, BillingPlan plan) {
    PaymentOrder order = new PaymentOrder();
    order.setId(UUID.randomUUID());
    order.setUserId(owner);
    order.setRazorpayOrderId("order_ABC");
    order.setPlan(plan);
    order.setAmountPaise(plan.amountPaise());
    order.setCurrency("INR");
    return order;
  }

  private static String capturedEvent(String orderId, String paymentId, int amount) {
    return "{\"event\":\"payment.captured\",\"payload\":{\"payment\":{\"entity\":{\"id\":\""
        + paymentId
        + "\",\"order_id\":\""
        + orderId
        + "\",\"amount\":"
        + amount
        + "}}}}";
  }
}
