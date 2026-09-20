package com.devtrack.ai.service;

import com.devtrack.ai.AiProvider;
import com.devtrack.ai.AiRequest;
import com.devtrack.ai.AiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * The ONLY class in this codebase that knows Gemini exists — its request/response shapes, headers,
 * and model name never leak past the AiProvider interface. Verified against Google's current (2026)
 * Interactions API directly rather than assumed from training data: the raw REST response has no
 * "output_text" field — that's an SDK-only convenience getter — and the API had already moved from
 * the older generateContent endpoint to POST /v1beta/interactions with an x-goog-api-key header
 * before this was written.
 *
 * <p>RestClient is built per-instance from the injected Builder, same pattern as
 * GithubOAuthService, not a shared bean — same testability reasoning noted there.
 */
@Service
@EnableConfigurationProperties(GeminiProperties.class)
public class GeminiAiProvider implements AiProvider {

  private static final Logger log = LoggerFactory.getLogger(GeminiAiProvider.class);
  private static final String INTERACTIONS_URL =
      "https://generativelanguage.googleapis.com/v1beta/interactions";
  private static final int MAX_ATTEMPTS = 3;
  private static final Duration BASE_BACKOFF = Duration.ofMillis(500);

  private final RestClient restClient;
  private final ObjectMapper objectMapper;
  private final GeminiProperties properties;

  public GeminiAiProvider(
      RestClient.Builder restClientBuilder,
      ObjectMapper objectMapper,
      GeminiProperties properties) {
    this.restClient = restClientBuilder.build();
    this.objectMapper = objectMapper;
    this.properties = properties;
  }

  @Override
  public AiResponse generate(AiRequest request) {
    ObjectNode body = objectMapper.createObjectNode();
    body.put("model", properties.model());
    body.put("system_instruction", request.systemInstruction());
    body.put("input", request.untrustedDataBlock());
    // No conversation history is kept for a one-shot scoring call — nothing here ever needs
    // previous_interaction_id, so there is no documented reason for Google to retain this
    // interaction server-side.
    body.put("store", false);

    ObjectNode responseFormat = body.putObject("response_format");
    responseFormat.put("type", "text");
    responseFormat.put("mime_type", "application/json");
    responseFormat.set("schema", objectMapper.valueToTree(request.responseJsonSchema()));

    for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
      try {
        JsonNode responseBody =
            restClient
                .post()
                .uri(INTERACTIONS_URL)
                .header("x-goog-api-key", properties.apiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        String text = extractModelOutputText(responseBody);
        if (text.isBlank()) {
          return AiResponse.failure("Gemini returned an empty response.");
        }
        return AiResponse.success(text);

      } catch (RestClientResponseException e) {
        int status = e.getStatusCode().value();
        boolean retryable = status == 429 || status >= 500;
        log.warn("Gemini call failed (attempt {}/{}, status {})", attempt, MAX_ATTEMPTS, status);
        if (!retryable || attempt == MAX_ATTEMPTS) {
          return AiResponse.failure("Gemini request failed with status " + status);
        }
        sleepWithBackoff(attempt);

      } catch (ResourceAccessException e) {
        log.warn("Gemini call network error (attempt {}/{})", attempt, MAX_ATTEMPTS, e);
        if (attempt == MAX_ATTEMPTS) {
          return AiResponse.failure("Could not reach Gemini: " + e.getMessage());
        }
        sleepWithBackoff(attempt);
      }
    }

    return AiResponse.failure("Gemini request failed after " + MAX_ATTEMPTS + " attempts.");
  }

  /**
   * Concatenates text from every "model_output" step's "text"-type content blocks, skipping
   * "thought" steps entirely — mirrors the official SDK's documented output_text behavior, which is
   * computed client-side and does not exist as a literal field in the raw REST response.
   */
  private String extractModelOutputText(JsonNode responseBody) {
    if (responseBody == null) {
      return "";
    }
    StringBuilder text = new StringBuilder();
    for (JsonNode step : responseBody.path("steps")) {
      if (!"model_output".equals(step.path("type").asText())) {
        continue;
      }
      for (JsonNode contentBlock : step.path("content")) {
        if ("text".equals(contentBlock.path("type").asText())) {
          text.append(contentBlock.path("text").asText());
        }
      }
    }
    return text.toString();
  }

  private void sleepWithBackoff(int attempt) {
    long backoffMillis = BASE_BACKOFF.toMillis() * (1L << (attempt - 1));
    long jitter = (long) (backoffMillis * 0.2 * ThreadLocalRandom.current().nextDouble());
    try {
      Thread.sleep(backoffMillis + jitter);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
