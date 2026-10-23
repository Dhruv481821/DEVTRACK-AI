package com.devtrack.billing.service;

import java.util.Map;

/** The only seam to Razorpay's REST API, so BillingService is testable without the network. */
public interface RazorpayClient {

  /** Creates a Razorpay order and returns its id (order_...). Throws on any API failure. */
  String createOrder(int amountPaise, String currency, String receipt, Map<String, String> notes);
}
