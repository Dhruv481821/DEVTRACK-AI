package com.devtrack.ai.service;

import com.devtrack.ai.AgentConfig;
import com.devtrack.ai.AiProvider;
import com.devtrack.ai.AiRequest;
import com.devtrack.ai.AiResponse;
import com.devtrack.ai.dto.response.CareerAssistantResponse;
import com.devtrack.ai.dto.response.CareerAssistantResult;
import com.devtrack.ai.exception.AiResponseValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * FR-AI-01 — the Career Agent (09_AI_Architecture.md §3). Answers a free-form career question from
 * data joined across modules (DSA, GitHub, resume, job applications, study, certificates, projects)
 * via CareerContextBuilder. Same pipeline as ResumeAtsService: exact cache → shared daily quota →
 * provider → validate → cache; on any failure, last-known-good for the same question, else
 * UNAVAILABLE (§8). Read-only: nothing here writes to any module (§4's blast-radius bound).
 */
@Service
public class CareerAssistantService {

  private static final Logger log = LoggerFactory.getLogger(CareerAssistantService.class);

  static final int MAX_ANSWER_CHARS = 4000;
  static final int MAX_RECOMMENDATION_CHARS = 300;

  /** Shape the model is asked to return (CareerAssistantAgentConfig's schema). */
  record ModelOutput(String answer, List<String> recommendations) {}

  private final CareerContextBuilder contextBuilder;
  private final AiProvider aiProvider;
  private final CareerAssistantAgentConfig agentConfigProvider;
  private final AiResponseCacheService cacheService;
  private final AiQuotaService aiQuotaService;
  private final ObjectMapper objectMapper;

  public CareerAssistantService(
      CareerContextBuilder contextBuilder,
      AiProvider aiProvider,
      CareerAssistantAgentConfig agentConfigProvider,
      AiResponseCacheService cacheService,
      AiQuotaService aiQuotaService,
      ObjectMapper objectMapper) {
    this.contextBuilder = contextBuilder;
    this.aiProvider = aiProvider;
    this.agentConfigProvider = agentConfigProvider;
    this.cacheService = cacheService;
    this.aiQuotaService = aiQuotaService;
    this.objectMapper = objectMapper;
  }

  public CareerAssistantResponse ask(UUID userId, String question) {
    String normalizedQuestion = question.trim();
    CareerContextBuilder.CareerContext context = contextBuilder.build(userId);

    // Content hash of the data block = sourceDataVersion (09_AI_Architecture.md §6): any change
    // to the underlying data produces a new key, so a stale answer is never served as fresh.
    String questionHash = sha256(normalizedQuestion.toLowerCase(Locale.ROOT));
    String exactKey =
        "ai-cache:exact:CAREER_ASSISTANT:"
            + userId
            + ":"
            + sha256(context.profileDataBlock())
            + ":"
            + questionHash;
    String lastKnownGoodKey = "ai-cache:last:CAREER_ASSISTANT:" + userId + ":" + questionHash;

    Optional<AiResponseCacheService.Cached<CareerAssistantResult>> cached =
        cacheService.get(exactKey, CareerAssistantResult.class);
    if (cached.isPresent()) {
      return CareerAssistantResponse.success(cached.get().result(), cached.get().generatedAt());
    }

    aiQuotaService.consumeOrThrow(userId);

    AgentConfig agentConfig = agentConfigProvider.get();
    AiRequest request =
        new AiRequest(
            agentConfig.systemPromptTemplate(),
            context.profileDataBlock() + buildQuestionBlock(normalizedQuestion),
            agentConfig.responseSchema());

    AiResponse aiResponse = aiProvider.generate(request);
    if (aiResponse.success()) {
      try {
        ModelOutput output = parseAndValidate(aiResponse.jsonPayload());
        CareerAssistantResult result =
            new CareerAssistantResult(output.answer(), output.recommendations(), context.basedOn());
        Instant generatedAt = Instant.now();
        cacheService.put(exactKey, result, generatedAt, agentConfig.cacheTtl());
        cacheService.put(lastKnownGoodKey, result, generatedAt, null);
        return CareerAssistantResponse.success(result, generatedAt);
      } catch (AiResponseValidationException e) {
        log.warn(e.getMessage(), e);
      }
    } else {
      log.warn("Career assistant provider call failed: {}", aiResponse.errorMessage());
    }

    return cacheService
        .get(lastKnownGoodKey, CareerAssistantResult.class)
        .map(c -> CareerAssistantResponse.cachedFallback(c.result(), c.generatedAt()))
        .orElseGet(
            () ->
                CareerAssistantResponse.unavailable(
                    "AI insights aren't available right now — the rest of your dashboard works"
                        + " normally."));
  }

  // The question is user input too: it goes in the untrusted block, never the system instruction.
  private String buildQuestionBlock(String question) {
    return "<question>\n" + CareerContextBuilder.sanitizeQuestion(question) + "\n</question>\n";
  }

  /** Model output is untrusted: it must parse AND satisfy content bounds, or it is discarded. */
  ModelOutput parseAndValidate(String jsonPayload) {
    ModelOutput output;
    try {
      output = objectMapper.readValue(jsonPayload, ModelOutput.class);
    } catch (Exception e) {
      throw new AiResponseValidationException(
          "Career assistant response did not match the expected schema", e);
    }
    if (output == null || output.answer() == null || output.answer().isBlank()) {
      throw new AiResponseValidationException("Career assistant response had no answer", null);
    }
    List<String> recommendations =
        output.recommendations() == null
            ? List.of()
            : output.recommendations().stream()
                .filter(r -> r != null && !r.isBlank())
                .map(r -> CareerContextBuilder.truncate(r.strip(), MAX_RECOMMENDATION_CHARS))
                .limit(CareerAssistantAgentConfig.MAX_RECOMMENDATIONS)
                .toList();
    return new ModelOutput(
        CareerContextBuilder.truncate(output.answer().strip(), MAX_ANSWER_CHARS), recommendations);
  }

  private static String sha256(String input) {
    try {
      byte[] hash =
          MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder();
      for (byte b : hash) {
        hex.append(String.format("%02x", b));
      }
      return hex.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}
