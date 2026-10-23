package com.devtrack.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.devtrack.ai.AiRequest;
import com.devtrack.ai.AiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class OllamaAiProviderTest {

  private static final String CHAT_URL = "http://localhost:11434/api/chat";

  private MockRestServiceServer server;
  private OllamaAiProvider provider;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
    OllamaProperties properties =
        new OllamaProperties("http://localhost:11434/", "qwen2.5:7b", 8192, Duration.ofSeconds(5));
    provider = new OllamaAiProvider(builder.build(), new ObjectMapper(), properties);
  }

  private AiRequest request() {
    return new AiRequest("system rules", "<data>resume</data>", Map.of("type", "object"));
  }

  @Test
  void generate_sendsSeparateSystemAndUserMessagesWithSchema_andReturnsContent() {
    server
        .expect(requestTo(CHAT_URL))
        .andExpect(method(HttpMethod.POST))
        .andExpect(jsonPath("$.model").value("qwen2.5:7b"))
        .andExpect(jsonPath("$.stream").value(false))
        .andExpect(jsonPath("$.messages[0].role").value("system"))
        .andExpect(jsonPath("$.messages[0].content").value("system rules"))
        .andExpect(jsonPath("$.messages[1].role").value("user"))
        .andExpect(jsonPath("$.messages[1].content").value("<data>resume</data>"))
        .andExpect(jsonPath("$.format.type").value("object"))
        .andExpect(jsonPath("$.options.num_ctx").value(8192))
        .andRespond(
            withSuccess(
                "{\"message\":{\"role\":\"assistant\",\"content\":\"{\\\"score\\\":72}\"},"
                    + "\"done\":true}",
                MediaType.APPLICATION_JSON));

    AiResponse response = provider.generate(request());

    assertThat(response.success()).isTrue();
    assertThat(response.jsonPayload()).isEqualTo("{\"score\":72}");
    server.verify();
  }

  @Test
  void generate_onServerError_returnsFailureInsteadOfThrowing() {
    server.expect(requestTo(CHAT_URL)).andRespond(withServerError());

    AiResponse response = provider.generate(request());

    assertThat(response.success()).isFalse();
  }

  @Test
  void generate_onEmptyContent_returnsFailure() {
    server
        .expect(requestTo(CHAT_URL))
        .andRespond(
            withSuccess(
                "{\"message\":{\"content\":\"\"},\"done\":true}", MediaType.APPLICATION_JSON));

    assertThat(provider.generate(request()).success()).isFalse();
  }
}
