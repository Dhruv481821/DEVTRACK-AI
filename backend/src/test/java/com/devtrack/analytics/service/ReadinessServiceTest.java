package com.devtrack.analytics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devtrack.analytics.entity.ReadinessSnapshot;
import com.devtrack.analytics.repository.ReadinessSnapshotRepository;
import com.devtrack.dsa.dto.response.DsaProblemResponse;
import com.devtrack.dsa.service.DsaProblemService;
import com.devtrack.github.service.GithubSummaryService;
import com.devtrack.resume.dto.response.ResumeResponse;
import com.devtrack.resume.dto.response.ResumeSectionResponse;
import com.devtrack.resume.service.ResumeSectionService;
import com.devtrack.resume.service.ResumeService;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReadinessServiceTest {

  private DsaProblemService dsaProblemService;
  private ResumeService resumeService;
  private ResumeSectionService resumeSectionService;
  private GithubSummaryService githubSummaryService;
  private ReadinessSnapshotRepository snapshotRepository;
  private ReadinessService service;

  private final UUID userId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    dsaProblemService = mock(DsaProblemService.class);
    resumeService = mock(ResumeService.class);
    resumeSectionService = mock(ResumeSectionService.class);
    githubSummaryService = mock(GithubSummaryService.class);
    snapshotRepository = mock(ReadinessSnapshotRepository.class);
    service =
        new ReadinessService(
            dsaProblemService,
            resumeService,
            resumeSectionService,
            githubSummaryService,
            snapshotRepository);

    when(githubSummaryService.activity(userId)).thenReturn(Optional.empty());
    when(snapshotRepository.findByUserIdAndSnapshotDateGreaterThanEqualOrderBySnapshotDateAsc(
            eq(userId), any(LocalDate.class)))
        .thenReturn(List.of());
  }

  @Test
  void getReadiness_scoresThreeComponentsAndUpsertsTodaysSnapshot() {
    when(dsaProblemService.listMyProblems(userId))
        .thenReturn(
            List.of(
                new DsaProblemResponse(
                    UUID.randomUUID(), "Two Sum", "EASY", List.of(), Instant.now()),
                new DsaProblemResponse(
                    UUID.randomUUID(), "LRU Cache", "MEDIUM", List.of(), Instant.now())));
    UUID resumeId = UUID.randomUUID();
    when(resumeService.listMyResumes(userId))
        .thenReturn(List.of(new ResumeResponse(resumeId, "Backend", Instant.now(), Instant.now())));
    when(resumeSectionService.listSectionsForResume(resumeId, userId))
        .thenReturn(
            List.of(
                new ResumeSectionResponse(UUID.randomUUID(), "SKILLS", Map.of("items", "Java"), 0),
                // Present but empty: must not count toward completeness.
                new ResumeSectionResponse(
                    UUID.randomUUID(), "EDUCATION", Map.of("school", " "), 1)));

    var response = service.getReadiness(userId);

    assertThat(response.components())
        .extracting(c -> c.key())
        .containsExactly("DSA", "RESUME", "GITHUB");
    assertThat(response.components().get(1).score()).isEqualTo(25);
    assertThat(response.components().get(2).score()).isZero();
    LocalDate today = LocalDate.now(ZoneOffset.UTC);
    verify(snapshotRepository)
        .upsert(
            eq(userId),
            eq(today),
            eq(response.overall()),
            eq(response.components().get(0).score()),
            eq(25),
            eq(0));
  }

  @Test
  void getReadiness_returnsStoredSnapshotsAsTheTrend() {
    when(dsaProblemService.listMyProblems(userId)).thenReturn(List.of());
    when(resumeService.listMyResumes(userId)).thenReturn(List.of());
    ReadinessSnapshot yesterday = new ReadinessSnapshot();
    yesterday.setSnapshotDate(LocalDate.now(ZoneOffset.UTC).minusDays(1));
    yesterday.setOverallScore(40);
    yesterday.setDsaScore(50);
    yesterday.setResumeScore(70);
    yesterday.setGithubScore(0);
    when(snapshotRepository.findByUserIdAndSnapshotDateGreaterThanEqualOrderBySnapshotDateAsc(
            eq(userId), any(LocalDate.class)))
        .thenReturn(List.of(yesterday));

    var response = service.getReadiness(userId);

    assertThat(response.overall()).isZero();
    assertThat(response.trend()).hasSize(1);
    assertThat(response.trend().get(0).overall()).isEqualTo(40);
  }
}
