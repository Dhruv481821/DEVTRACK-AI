package com.devtrack.analytics.service;

import com.devtrack.analytics.dto.response.ReadinessResponse;
import com.devtrack.analytics.repository.ReadinessSnapshotRepository;
import com.devtrack.dsa.dto.response.DsaProblemResponse;
import com.devtrack.dsa.service.DsaProblemService;
import com.devtrack.github.dto.response.GithubActivityResponse;
import com.devtrack.github.service.GithubSummaryService;
import com.devtrack.resume.dto.response.ResumeResponse;
import com.devtrack.resume.dto.response.ResumeSectionResponse;
import com.devtrack.resume.service.ResumeSectionService;
import com.devtrack.resume.service.ResumeService;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * FR-ANALYTICS-01. Reads DSA, resume and GitHub data through those modules' services (never their
 * repositories), scores it with ReadinessCalculator, records today's snapshot (idempotent upsert)
 * and returns the score with its 90-day trend. No AI call, so it costs no AI quota.
 */
@Service
public class ReadinessService {

  static final int TREND_DAYS = 90;

  private final DsaProblemService dsaProblemService;
  private final ResumeService resumeService;
  private final ResumeSectionService resumeSectionService;
  private final GithubSummaryService githubSummaryService;
  private final ReadinessSnapshotRepository snapshotRepository;

  public ReadinessService(
      DsaProblemService dsaProblemService,
      ResumeService resumeService,
      ResumeSectionService resumeSectionService,
      GithubSummaryService githubSummaryService,
      ReadinessSnapshotRepository snapshotRepository) {
    this.dsaProblemService = dsaProblemService;
    this.resumeService = resumeService;
    this.resumeSectionService = resumeSectionService;
    this.githubSummaryService = githubSummaryService;
    this.snapshotRepository = snapshotRepository;
  }

  @Transactional
  public ReadinessResponse getReadiness(UUID userId) {
    LocalDate today = LocalDate.now(ZoneOffset.UTC);

    ReadinessResponse.Component dsa = ReadinessCalculator.dsa(dsaInput(userId, today));
    ReadinessResponse.Component resume = ReadinessCalculator.resume(resumeInput(userId));
    ReadinessResponse.Component github = ReadinessCalculator.github(githubInput(userId));
    List<ReadinessResponse.Component> components = List.of(dsa, resume, github);
    int overall = ReadinessCalculator.overall(components);

    snapshotRepository.upsert(userId, today, overall, dsa.score(), resume.score(), github.score());

    List<ReadinessResponse.TrendPoint> trend =
        snapshotRepository
            .findByUserIdAndSnapshotDateGreaterThanEqualOrderBySnapshotDateAsc(
                userId, today.minusDays(TREND_DAYS - 1))
            .stream()
            .map(
                s ->
                    new ReadinessResponse.TrendPoint(
                        s.getSnapshotDate(),
                        s.getOverallScore(),
                        s.getDsaScore(),
                        s.getResumeScore(),
                        s.getGithubScore()))
            .toList();

    return new ReadinessResponse(overall, components, trend, today);
  }

  private ReadinessCalculator.DsaInput dsaInput(UUID userId, LocalDate today) {
    List<DsaProblemResponse> problems = dsaProblemService.listMyProblems(userId);
    int mediumOrHard =
        (int)
            problems.stream()
                .filter(p -> "MEDIUM".equals(p.difficulty()) || "HARD".equals(p.difficulty()))
                .count();
    List<LocalDate> dates =
        problems.stream()
            .map(DsaProblemResponse::createdAt)
            .filter(Objects::nonNull)
            .map(instant -> instant.atZone(ZoneOffset.UTC).toLocalDate())
            .toList();
    return new ReadinessCalculator.DsaInput(problems.size(), mediumOrHard, dates, today);
  }

  // Scores the most recently updated resume — the one the user is actively working on.
  private ReadinessCalculator.ResumeInput resumeInput(UUID userId) {
    Optional<ResumeResponse> latest =
        resumeService.listMyResumes(userId).stream()
            .max(Comparator.comparing(ResumeResponse::updatedAt));
    if (latest.isEmpty()) {
      return new ReadinessCalculator.ResumeInput(null, Set.of());
    }
    Set<String> withContent =
        resumeSectionService.listSectionsForResume(latest.get().id(), userId).stream()
            .filter(s -> s.content() != null && hasContent(s))
            .map(ResumeSectionResponse::sectionType)
            .collect(Collectors.toSet());
    return new ReadinessCalculator.ResumeInput(latest.get().title(), withContent);
  }

  private static boolean hasContent(ResumeSectionResponse section) {
    return section.content().values().stream()
        .anyMatch(v -> v != null && !String.valueOf(v).isBlank());
  }

  private ReadinessCalculator.GithubInput githubInput(UUID userId) {
    Optional<GithubActivityResponse> activity = githubSummaryService.activity(userId);
    return activity
        .map(
            a ->
                new ReadinessCalculator.GithubInput(
                    true, a.commitsLast90Days(), a.activeRepos(), a.languageCount()))
        .orElseGet(() -> new ReadinessCalculator.GithubInput(false, 0, 0, 0));
  }
}
