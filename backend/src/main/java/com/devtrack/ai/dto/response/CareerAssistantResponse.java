package com.devtrack.ai.dto.response;

import java.time.Instant;

/** result/generatedAt are null for UNAVAILABLE; message is null otherwise. */
public record CareerAssistantResponse(
    CareerAssistantStatus status,
    CareerAssistantResult result,
    Instant generatedAt,
    String message) {

  public static CareerAssistantResponse success(CareerAssistantResult result, Instant generatedAt) {
    return new CareerAssistantResponse(CareerAssistantStatus.SUCCESS, result, generatedAt, null);
  }

  public static CareerAssistantResponse cachedFallback(
      CareerAssistantResult result, Instant generatedAt) {
    return new CareerAssistantResponse(
        CareerAssistantStatus.CACHED_FALLBACK, result, generatedAt, null);
  }

  public static CareerAssistantResponse unavailable(String message) {
    return new CareerAssistantResponse(CareerAssistantStatus.UNAVAILABLE, null, null, message);
  }
}
