package com.devtrack.billing.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Razorpay Orders API (POST https://api.razorpay.com/v1/orders) with HTTP Basic auth key_id:secret.
 * Same RestClient pattern as GithubApiClient. The secret is only ever used for this header.
 */
@Service
@EnableConfigurationProperties(RazorpayProperties.class)
public class RazorpayApiClient implements RazorpayClient {

  private static final String ORDERS_URL = "https://api.razorpay.com/v1/orders";

  private final RestClient restClient;
  private final RazorpayProperties properties;

  public RazorpayApiClient(RestClient.Builder restClientBuilder, RazorpayProperties properties) {
    this.restClient = restClientBuilder.build();
    this.properties = properties;
  }

  @Override
  @SuppressWarnings("unchecked")
  public String createOrder(
      int amountPaise, String currency, String receipt, Map<String, String> notes) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("amount", amountPaise);
    body.put("currency", currency);
    body.put("receipt", receipt);
    body.put("notes", notes);

    String credentials = properties.keyId() + ":" + properties.keySecret();
    String basicAuth =
        "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

    Map<String, Object> response =
        restClient
            .post()
            .uri(ORDERS_URL)
            .header(HttpHeaders.AUTHORIZATION, basicAuth)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .body(Map.class);

    Object id = response == null ? null : response.get("id");
    if (!(id instanceof String orderId) || orderId.isBlank()) {
      throw new IllegalStateException("Razorpay order response had no id");
    }
    return orderId;
  }
}
