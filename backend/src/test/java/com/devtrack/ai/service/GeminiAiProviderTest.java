package com.devtrack.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.devtrack.ai.AiRequest;
import com.devtrack.ai.AiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/**
 * UNVERIFIED: MockRestServiceServer.bindTo(RestClient.Builder) is used below but was not confirmed
 * against the real Spring Framework 6.1.13 API — the official reference docs only show RestTemplate
 * examples and recommend WireMock/OkHttp MockWebServer for RestClient instead. If this doesn't
 * compile, that confirms it and this file needs rewriting against whichever approach actually works
 * — do not assume this test is correct just because it's here.
 *
 * <p>The 429-retry test genuinely sleeps through real backoff delays (~1.5s total) rather than
 * mocking time, since Thread.sleep wasn't made a testability seam for this feature.
 */
class GeminiAiProviderTest {

  private static final String INTERACTIONS_URL =
      "https://generativelanguage.googleapis.com/v1beta/interactions";

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void generate_onModelOutputStep_extractsTextSkippingThoughtSteps() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

    String rawResponse =
        """
        {
          "id": "int_123",
          "status": "completed",
          "steps": [
            {"type": "thought", "signature": "abc"},
            {"type": "model_output", "content": [{"type": "text", "text": "{\\"score\\":80}"}]}
          ]
        }""";

    server
        .expect(requestTo(INTERACTIONS_URL))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("x-goog-api-key", "test-key"))
        .andRespond(withSuccess(rawResponse, MediaType.APPLICATION_JSON));

    GeminiProperties properties = new GeminiProperties("test-key", "gemini-3.8-flash");
    GeminiAiProvider provider = new GeminiAiProvider(builder, objectMapper, properties);

    AiResponse response =
        provider.generate(new AiRequest("system", "data", Map.of("type", "object")));

    assertThat(response.success()).isTrue();
    assertThat(response.jsonPayload()).isEqualTo("{\"score\":80}");
    server.verify();
  }

  @Test
  void generate_on429ExhaustingRetries_returnsFailureWithoutThrowing() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

    for (int i = 0; i < 3; i++) {
      server
          .expect(requestTo(INTERACTIONS_URL))
          .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
    }

    GeminiProperties properties = new GeminiProperties("test-key", "gemini-3.8-flash");
    GeminiAiProvider provider = new GeminiAiProvider(builder, objectMapper, properties);

    AiResponse response =
        provider.generate(new AiRequest("system", "data", Map.of("type", "object")));

    assertThat(response.success()).isFalse();
    server.verify();
  }

  @Test
  void generate_onNonRetryable400_failsImmediatelyWithoutRetrying() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

    server.expect(requestTo(INTERACTIONS_URL)).andRespond(withStatus(HttpStatus.BAD_REQUEST));

    GeminiProperties properties = new GeminiProperties("test-key", "gemini-3.8-flash");
    GeminiAiProvider provider = new GeminiAiProvider(builder, objectMapper, properties);

    AiResponse response =
        provider.generate(new AiRequest("system", "data", Map.of("type", "object")));

    assertThat(response.success()).isFalse();
    server.verify();
  }
}
