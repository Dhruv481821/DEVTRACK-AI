package com.devtrack.github.service;

import com.devtrack.github.dto.response.GithubActivityResponse;
import com.devtrack.github.dto.response.GithubSummaryResponse;
import com.devtrack.github.entity.GithubConnection;
import com.devtrack.github.entity.RepoSnapshot;
import com.devtrack.github.repository.GithubConnectionRepository;
import com.devtrack.github.repository.RepoSnapshotRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GithubSummaryService {

  private static final int TOP_LANGUAGE_COUNT = 5;

  private final GithubConnectionRepository githubConnectionRepository;
  private final RepoSnapshotRepository repoSnapshotRepository;

  public GithubSummaryService(
      GithubConnectionRepository githubConnectionRepository,
      RepoSnapshotRepository repoSnapshotRepository) {
    this.githubConnectionRepository = githubConnectionRepository;
    this.repoSnapshotRepository = repoSnapshotRepository;
  }

  @Transactional(readOnly = true)
  public Optional<GithubSummaryResponse> summarize(UUID userId) {
    return githubConnectionRepository
        .findByUserId(userId)
        .map(GithubConnection::getId)
        .map(repoSnapshotRepository::findByGithubConnectionId)
        .map(this::toSummary);
  }

  /** Empty when the user never connected GitHub. activeRepos = repos with commits in 90 days. */
  @Transactional(readOnly = true)
  public Optional<GithubActivityResponse> activity(UUID userId) {
    return githubConnectionRepository
        .findByUserId(userId)
        .map(GithubConnection::getId)
        .map(repoSnapshotRepository::findByGithubConnectionId)
        .map(
            repos ->
                new GithubActivityResponse(
                    repos.size(),
                    repos.stream().mapToInt(RepoSnapshot::getCommitsLast90Days).sum(),
                    (int) repos.stream().filter(r -> r.getCommitsLast90Days() > 0).count(),
                    (int)
                        repos.stream()
                            .map(RepoSnapshot::getPrimaryLanguage)
                            .filter(language -> language != null && !language.isBlank())
                            .distinct()
                            .count()));
  }

  private GithubSummaryResponse toSummary(List<RepoSnapshot> repos) {
    int totalStars = repos.stream().mapToInt(RepoSnapshot::getStars).sum();

    Map<String, Long> repoCountByLanguage =
        repos.stream()
            .map(RepoSnapshot::getPrimaryLanguage)
            .filter(language -> language != null && !language.isBlank())
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

    List<String> topLanguages =
        repoCountByLanguage.entrySet().stream()
            .sorted(
                Map.Entry.<String, Long>comparingByValue()
                    .reversed()
                    .thenComparing(Map.Entry.comparingByKey(Comparator.naturalOrder())))
            .limit(TOP_LANGUAGE_COUNT)
            .map(Map.Entry::getKey)
            .toList();

    return new GithubSummaryResponse(repos.size(), totalStars, topLanguages);
  }
}
