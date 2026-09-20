package com.devtrack.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.devtrack.ai.AgentConfig;
import com.devtrack.ai.AgentType;
import com.devtrack.ai.AiProperties;
import com.devtrack.ai.AiProvider;
import com.devtrack.ai.AiRequest;
import com.devtrack.ai.AiResponse;
import com.devtrack.ai.dto.response.AtsScoreResponse;
import com.devtrack.ai.dto.response.AtsScoreResult;
import com.devtrack.ai.dto.response.AtsScoreStatus;
import com.devtrack.common.exception.AiQuotaExceededException;
import com.devtrack.common.security.RateLimiterService;
import com.devtrack.resume.dto.response.ResumeResponse;
import com.devtrack.resume.dto.response.ResumeSectionResponse;
import com.devtrack.resume.service.ResumeSectionService;
import com.devtrack.resume.service.ResumeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResumeAtsServiceTest {

  private ResumeService resumeService;
  private ResumeSectionService resumeSectionService;
  private AiProvider aiProvider;
  private ResumeAtsAgentConfig agentConfigProvider;
  private AiResponseCacheService cacheService;
  private RateLimiterService rateLimiterService;
  private ResumeAtsService resumeAtsService;

  private final UUID userId = UUID.randomUUID();
  private final UUID resumeId = UUID.randomUUID();
  private final String jobDescription = "Senior backend engineer, Java and Spring Boot required.";

  @BeforeEach
  void setUp() {
    resumeService = mock(ResumeService.class);
    resumeSectionService = mock(ResumeSectionService.class);
    aiProvider = mock(AiProvider.class);
    agentConfigProvider = mock(ResumeAtsAgentConfig.class);
    cacheService = mock(AiResponseCacheService.class);
    rateLimiterService = mock(RateLimiterService.class);
    AiProperties aiProperties = new AiProperties(20);
    ObjectMapper objectMapper = new ObjectMapper();

    resumeAtsService =
        new ResumeAtsService(
            resumeService,
            resumeSectionService,
            aiProvider,
            agentConfigProvider,
            cacheService,
            rateLimiterService,
            aiProperties,
            objectMapper);

    ResumeResponse resume =
        new ResumeResponse(resumeId, "Backend Engineer Resume", Instant.now(), Instant.now());
    List<ResumeSectionResponse> sections =
        List.of(
            new ResumeSectionResponse(
                UUID.randomUUID(), "EXPERIENCE", Map.of("company", "Acme"), 0));
    AgentConfig agentConfig =
        new AgentConfig(
            AgentType.RESUME_ATS,
            "system instruction",
            Map.of("type", "object"),
            Duration.ofHours(24));

    when(resumeService.getMyResume(resumeId, userId)).thenReturn(resume);
    when(resumeSectionService.listSectionsForResume(resumeId, userId)).thenReturn(sections);
    when(agentConfigProvider.get()).thenReturn(agentConfig);
  }

  @Test
  void scoreResume_onExactCacheHit_returnsSuccessWithoutCallingProviderOrQuota() {
    AtsScoreResult cachedResult =
        new AtsScoreResult(
            80, List.of("Java"), List.of(), List.of(), "Good match", List.of("resume"));
    Instant cachedAt = Instant.now();
    when(cacheService.getExact(anyString()))
        .thenReturn(Optional.of(new AiResponseCacheService.CachedResult(cachedResult, cachedAt)));

    AtsScoreResponse response = resumeAtsService.scoreResume(userId, resumeId, jobDescription);

    assertThat(response.status()).isEqualTo(AtsScoreStatus.SUCCESS);
    assertThat(response.result()).isEqualTo(cachedResult);
    verifyNoInteractions(aiProvider);
    verifyNoInteractions(rateLimiterService);
  }

  @Test
  void scoreResume_onCacheMissAndQuotaExceeded_throwsAiQuotaExceeded() {
    when(cacheService.getExact(anyString())).thenReturn(Optional.empty());
    when(rateLimiterService.tryConsumeDaily(anyString(), eq(20))).thenReturn(false);

    assertThatThrownBy(() -> resumeAtsService.scoreResume(userId, resumeId, jobDescription))
        .isInstanceOf(AiQuotaExceededException.class);

    verifyNoInteractions(aiProvider);
  }

  @Test
  void scoreResume_onProviderSuccess_cachesAndReturnsSuccess() {
    when(cacheService.getExact(anyString())).thenReturn(Optional.empty());
    when(rateLimiterService.tryConsumeDaily(anyString(), eq(20))).thenReturn(true);

    String json =
        """
        {"score":85,"matchedKeywords":["Java","Spring Boot"],"missingKeywords":["Kubernetes"],\
        "formattingIssues":[],"reasoning":"Strong match","sourcesUsed":["resume","job description"]}\
        """;
    when(aiProvider.generate(any(AiRequest.class))).thenReturn(AiResponse.success(json));
    doNothing()
        .when(cacheService)
        .putExact(anyString(), any(AtsScoreResult.class), any(Instant.class), any(Duration.class));
    doNothing()
        .when(cacheService)
        .putLastKnownGood(anyString(), any(AtsScoreResult.class), any(Instant.class));

    AtsScoreResponse response = resumeAtsService.scoreResume(userId, resumeId, jobDescription);

    assertThat(response.status()).isEqualTo(AtsScoreStatus.SUCCESS);
    assertThat(response.result().score()).isEqualTo(85);
    verify(cacheService)
        .putExact(
            anyString(), any(AtsScoreResult.class), any(Instant.class), eq(Duration.ofHours(24)));
    verify(cacheService)
        .putLastKnownGood(anyString(), any(AtsScoreResult.class), any(Instant.class));
  }

  @Test
  void scoreResume_onProviderFailureWithNoFallback_returnsUnavailable() {
    when(cacheService.getExact(anyString())).thenReturn(Optional.empty());
    when(rateLimiterService.tryConsumeDaily(anyString(), eq(20))).thenReturn(true);
    when(aiProvider.generate(any(AiRequest.class))).thenReturn(AiResponse.failure("network error"));
    when(cacheService.getLastKnownGood(anyString())).thenReturn(Optional.empty());

    AtsScoreResponse response = resumeAtsService.scoreResume(userId, resumeId, jobDescription);

    assertThat(response.status()).isEqualTo(AtsScoreStatus.UNAVAILABLE);
    assertThat(response.result()).isNull();
    assertThat(response.message()).isNotBlank();
  }

  @Test
  void scoreResume_onProviderFailureWithFallbackAvailable_returnsCachedFallback() {
    when(cacheService.getExact(anyString())).thenReturn(Optional.empty());
    when(rateLimiterService.tryConsumeDaily(anyString(), eq(20))).thenReturn(true);
    when(aiProvider.generate(any(AiRequest.class))).thenReturn(AiResponse.failure("network error"));

    AtsScoreResult previousResult =
        new AtsScoreResult(
            70, List.of("Java"), List.of("Docker"), List.of(), "Ok match", List.of("resume"));
    Instant previousGeneratedAt = Instant.now().minusSeconds(3600);
    when(cacheService.getLastKnownGood(anyString()))
        .thenReturn(
            Optional.of(
                new AiResponseCacheService.CachedResult(previousResult, previousGeneratedAt)));

    AtsScoreResponse response = resumeAtsService.scoreResume(userId, resumeId, jobDescription);

    assertThat(response.status()).isEqualTo(AtsScoreStatus.CACHED_FALLBACK);
    assertThat(response.result()).isEqualTo(previousResult);
    assertThat(response.generatedAt()).isEqualTo(previousGeneratedAt);
  }

  @Test
  void scoreResume_onMalformedProviderResponse_fallsBackLikeAFailure() {
    when(cacheService.getExact(anyString())).thenReturn(Optional.empty());
    when(rateLimiterService.tryConsumeDaily(anyString(), eq(20))).thenReturn(true);
    when(aiProvider.generate(any(AiRequest.class)))
        .thenReturn(AiResponse.success("not valid json"));
    when(cacheService.getLastKnownGood(anyString())).thenReturn(Optional.empty());

    AtsScoreResponse response = resumeAtsService.scoreResume(userId, resumeId, jobDescription);

    assertThat(response.status()).isEqualTo(AtsScoreStatus.UNAVAILABLE);
  }
}
