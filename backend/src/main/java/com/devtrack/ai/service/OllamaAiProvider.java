package com.devtrack.ai.service;

import com.devtrack.ai.AiProvider;
import com.devtrack.ai.AiRequest;
import com.devtrack.ai.AiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Local, zero-cost AI provider for development: a model running in Ollama on the developer's own
 * machine (POST {baseUrl}/api/chat, non-streaming, JSON-schema structured output via "format").
 * Active only when devtrack.ai.provider=ollama; production uses GeminiAiProvider.
 *
 * <p>Keeps the AiRequest separation intact: the trusted instruction is the "system" message and the
 * untrusted data block is the "user" message — never concatenated into one string.
 */
@Service
@ConditionalOnProperty(name = "devtrack.ai.provider", havingValue = "ollama")
@EnableConfigurationProperties(OllamaProperties.class)
public class OllamaAiProvider implements AiProvider {

  private static final Logger log = LoggerFactory.getLogger(OllamaAiProvider.class);

  private final RestClient restClient;
  private final ObjectMapper objectMapper;
  private final OllamaProperties properties;

  @Autowired
  public OllamaAiProvider(
      RestClient.Builder restClientBuilder,
      ObjectMapper objectMapper,
      OllamaProperties properties) {
    this(
        restClientBuilder.requestFactory(requestFactory(properties)).build(),
        objectMapper,
        properties);
  }

  // Package-private: lets tests bind a mock server without the timeout factory replacing it.
  OllamaAiProvider(RestClient restClient, ObjectMapper objectMapper, OllamaProperties properties) {
    this.restClient = restClient;
    this.objectMapper = objectMapper;
    this.properties = properties;
  }

  private static SimpleClientHttpRequestFactory requestFactory(OllamaProperties properties) {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(5_000);
    factory.setReadTimeout((int) properties.timeout().toMillis());
    return factory;
  }

  @Override
  public AiResponse generate(AiRequest request) {
    ObjectNode body = objectMapper.createObjectNode();
    body.put("model", properties.model());
    body.put("stream", false);
    ArrayNode messages = body.putArray("messages");
    messages.addObject().put("role", "system").put("content", request.systemInstruction());
    messages.addObject().put("role", "user").put("content", request.untrustedDataBlock());
    body.set("format", objectMapper.valueToTree(request.responseJsonSchema()));
    ObjectNode options = body.putObject("options");
    options.put("temperature", 0);
    options.put("num_ctx", properties.contextWindow());

    String url = properties.baseUrl().replaceAll("/+$", "") + "/api/chat";
    try {
      JsonNode response =
          restClient
              .post()
              .uri(url)
              .contentType(MediaType.APPLICATION_JSON)
              .body(body)
              .retrieve()
              .body(JsonNode.class);

      String text = response == null ? "" : response.path("message").path("content").asText("");
      if (text.isBlank()) {
        return AiResponse.failure("Ollama returned an empty response.");
      }
      return AiResponse.success(text);

    } catch (RestClientResponseException e) {
      // 404 here usually means the model isn't pulled: run `ollama pull <model>`.
      log.warn(
          "Ollama call failed with status {} for model {}",
          e.getStatusCode().value(),
          properties.model());
      return AiResponse.failure("Ollama request failed with status " + e.getStatusCode().value());
    } catch (ResourceAccessException e) {
      log.warn("Could not reach Ollama at {} — is it running?", properties.baseUrl());
      return AiResponse.failure("Could not reach Ollama at " + properties.baseUrl());
    }
  }
}
