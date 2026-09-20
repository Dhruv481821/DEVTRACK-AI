package com.devtrack.ai.service;

import com.devtrack.ai.AgentConfig;
import com.devtrack.ai.AiProperties;
import com.devtrack.ai.AiProvider;
import com.devtrack.ai.AiRequest;
import com.devtrack.ai.AiResponse;
import com.devtrack.ai.dto.response.AtsScoreResponse;
import com.devtrack.ai.dto.response.AtsScoreResult;
import com.devtrack.ai.exception.AiResponseValidationException;
import com.devtrack.common.exception.AiQuotaExceededException;
import com.devtrack.common.security.RateLimiterService;
import com.devtrack.resume.dto.response.ResumeResponse;
import com.devtrack.resume.dto.response.ResumeSectionResponse;
import com.devtrack.resume.service.ResumeSectionService;
import com.devtrack.resume.service.ResumeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

/**
 * The single point of contact between the Resume module and the AI module. ResumeService and
 * ResumeSectionService gain zero new imports or knowledge of AI concerns anywhere — everything
 * Resume-specific about ATS scoring lives here instead. This class only calls their existing PUBLIC
 * methods (getMyResume, listSectionsForResume), both of which already enforce ownership internally;
 * no package-private helper (findOrThrow/assertOwned) was made public for this, and none needed to
 * be.
 */
@Service
@EnableConfigurationProperties(AiProperties.class)
public class ResumeAtsService {

  private static final Logger log = LoggerFactory.getLogger(ResumeAtsService.class);
  private static final String QUOTA_KEY_PREFIX = "ai-quota:";

  private final ResumeService resumeService;
  private final ResumeSectionService resumeSectionService;
  private final AiProvider aiProvider;
  private final ResumeAtsAgentConfig agentConfigProvider;
  private final AiResponseCacheService cacheService;
  private final RateLimiterService rateLimiterService;
  private final AiProperties aiProperties;
  private final ObjectMapper objectMapper;

  public ResumeAtsService(
      ResumeService resumeService,
      ResumeSectionService resumeSectionService,
      AiProvider aiProvider,
      ResumeAtsAgentConfig agentConfigProvider,
      AiResponseCacheService cacheService,
      RateLimiterService rateLimiterService,
      AiProperties aiProperties,
      ObjectMapper objectMapper) {
    this.resumeService = resumeService;
    this.resumeSectionService = resumeSectionService;
    this.aiProvider = aiProvider;
    this.agentConfigProvider = agentConfigProvider;
    this.cacheService = cacheService;
    this.rateLimiterService = rateLimiterService;
    this.aiProperties = aiProperties;
    this.objectMapper = objectMapper;
  }

  public AtsScoreResponse scoreResume(UUID userId, UUID resumeId, String jobDescription) {
    ResumeResponse resume = resumeService.getMyResume(resumeId, userId);
    List<ResumeSectionResponse> sections =
        resumeSectionService.listSectionsForResume(resumeId, userId);

    String jobDescriptionHash = sha256(jobDescription.trim());
    String sourceDataVersion = computeSourceDataVersion(resume, sections);
    String exactCacheKey = buildExactCacheKey(userId, sourceDataVersion, jobDescriptionHash);
    String lastKnownGoodKey = buildLastKnownGoodKey(userId, resumeId, jobDescriptionHash);

    Optional<AiResponseCacheService.CachedResult> cachedExact =
        cacheService.getExact(exactCacheKey);
    if (cachedExact.isPresent()) {
      return AtsScoreResponse.success(cachedExact.get().result(), cachedExact.get().generatedAt());
    }

    // RateLimiterService knows nothing about "AI" — this class supplies the scope key and the
    // limit (from devtrack.ai.daily-request-limit, pre-existing before this feature was built).
    // Keeping that generic avoids a shared/common class depending on one feature's config
    // namespace.
    boolean allowed =
        rateLimiterService.tryConsumeDaily(buildQuotaKey(userId), aiProperties.dailyRequestLimit());
    if (!allowed) {
      throw new AiQuotaExceededException(
          "You've reached today's limit of AI requests. Try again later.");
    }

    AgentConfig agentConfig = agentConfigProvider.get();
    String untrustedDataBlock = buildUntrustedDataBlock(resume, sections, jobDescription);

    AiRequest aiRequest =
        new AiRequest(
            agentConfig.systemPromptTemplate(), untrustedDataBlock, agentConfig.responseSchema());

    AiResponse aiResponse = aiProvider.generate(aiRequest);

    if (aiResponse.success()) {
      try {
        AtsScoreResult result = parseResult(aiResponse.jsonPayload(), resumeId);
        Instant generatedAt = Instant.now();
        cacheService.putExact(exactCacheKey, result, generatedAt, agentConfig.cacheTtl());
        cacheService.putLastKnownGood(lastKnownGoodKey, result, generatedAt);
        return AtsScoreResponse.success(result, generatedAt);
      } catch (AiResponseValidationException e) {
        log.warn(e.getMessage(), e);
      }
    } else {
      log.warn("Gemini call failed for resume {}: {}", resumeId, aiResponse.errorMessage());
    }

    return fallbackOrUnavailable(lastKnownGoodKey);
  }

  private AtsScoreResult parseResult(String jsonPayload, UUID resumeId) {
    try {
      return objectMapper.readValue(jsonPayload, AtsScoreResult.class);
    } catch (Exception e) {
      throw new AiResponseValidationException(
          "Gemini response for resume " + resumeId + " did not match the expected schema", e);
    }
  }

  private AtsScoreResponse fallbackOrUnavailable(String lastKnownGoodKey) {
    Optional<AiResponseCacheService.CachedResult> lastKnownGood =
        cacheService.getLastKnownGood(lastKnownGoodKey);
    if (lastKnownGood.isPresent()) {
      return AtsScoreResponse.cachedFallback(
          lastKnownGood.get().result(), lastKnownGood.get().generatedAt());
    }
    return AtsScoreResponse.unavailable(
        "AI insights aren't available right now — the rest of your dashboard works normally.");
  }

  // Date-scoped so a fresh bucket appears each UTC day, rather than relying on Bucket4j's own
  // refill schedule (based on bucket creation time) to land on a calendar boundary — see
  // GlobalExceptionHandler.handleAiQuotaExceeded for where "seconds until reset" is computed from
  // this same UTC-midnight boundary.
  private String buildQuotaKey(UUID userId) {
    return QUOTA_KEY_PREFIX + userId + ":" + LocalDate.now(ZoneOffset.UTC);
  }

  /**
   * A content hash, not a timestamp hash — ResumeSectionResponse (the only thing this module is
   * allowed to see) has no updatedAt field, only ResumeResponse does. Hashing the actual returned
   * content instead means zero changes were needed to Resume's public DTOs, and it catches any real
   * difference rather than trusting updatedAt was bumped correctly on every write path.
   */
  private String computeSourceDataVersion(
      ResumeResponse resume, List<ResumeSectionResponse> sections) {
    StringBuilder raw = new StringBuilder();
    raw.append(resume.title()).append('|').append(resume.updatedAt());
    sections.stream()
        .sorted(Comparator.comparing(ResumeSectionResponse::id))
        .forEach(
            section ->
                raw.append('|')
                    .append(section.id())
                    .append(':')
                    .append(section.sectionType())
                    .append(':')
                    .append(section.orderIndex())
                    .append(':')
                    .append(toStableJson(section.content())));
    return sha256(raw.toString());
  }

  // TreeMap forces a deterministic key order before hashing — otherwise two logically-identical
  // content maps could hash differently purely from insertion-order differences, causing
  // unnecessary cache misses (not a correctness bug, but a pointless one to leave in).
  private String toStableJson(Map<String, Object> content) {
    try {
      return objectMapper.writeValueAsString(new TreeMap<>(content));
    } catch (Exception e) {
      throw new IllegalStateException("Failed to serialize section content for hashing", e);
    }
  }

  private String buildExactCacheKey(
      UUID userId, String sourceDataVersion, String jobDescriptionHash) {
    return "ai-cache:exact:RESUME_ATS:"
        + userId
        + ":"
        + sourceDataVersion
        + ":"
        + jobDescriptionHash;
  }

  // Includes jobDescriptionHash so a result generated for one job description can never be
  // returned as the "last known good" fallback for a different one.
  private String buildLastKnownGoodKey(UUID userId, UUID resumeId, String jobDescriptionHash) {
    return "ai-cache:last:RESUME_ATS:" + userId + ":" + resumeId + ":" + jobDescriptionHash;
  }

  /**
   * The prompt-injection mitigation itself (telling the model to treat this as data, not
   * instructions) lives in AgentConfig.systemPromptTemplate, not here — this method only builds the
   * untrusted data block those instructions refer to.
   */
  private String buildUntrustedDataBlock(
      ResumeResponse resume, List<ResumeSectionResponse> sections, String jobDescription) {
    StringBuilder block = new StringBuilder();
    block.append("<resume title=\"").append(escapeForTag(resume.title())).append("\">\n");
    for (ResumeSectionResponse section : sections) {
      block.append("  <section type=\"").append(section.sectionType()).append("\">\n");
      section
          .content()
          .forEach(
              (key, value) ->
                  block.append("    ").append(key).append(": ").append(value).append('\n'));
      block.append("  </section>\n");
    }
    block.append("</resume>\n");
    block.append("<job_description>\n").append(jobDescription).append("\n</job_description>\n");
    return block.toString();
  }

  private String escapeForTag(String value) {
    return value == null ? "" : value.replace("\"", "'");
  }

  private String sha256(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
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
