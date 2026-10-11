package com.devtrack.github.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.devtrack.github.entity.GithubConnection;
import com.devtrack.github.entity.RepoSnapshot;
import com.devtrack.github.repository.GithubConnectionRepository;
import com.devtrack.github.repository.RepoSnapshotRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GithubSummaryServiceTest {

  private GithubConnectionRepository githubConnectionRepository;
  private RepoSnapshotRepository repoSnapshotRepository;
  private GithubSummaryService githubSummaryService;

  private final UUID userId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    githubConnectionRepository = mock(GithubConnectionRepository.class);
    repoSnapshotRepository = mock(RepoSnapshotRepository.class);

    githubSummaryService =
        new GithubSummaryService(
            githubConnectionRepository,
            repoSnapshotRepository);
  }

  @Test
  void summarize_withoutConnection_returnsEmpty() {
    when(githubConnectionRepository.findByUserId(userId))
        .thenReturn(Optional.empty());

    assertThat(githubSummaryService.summarize(userId)).isEmpty();
  }

  @Test
  void summarize_aggregatesStarsAndRanksLanguagesByRepoCount() {
    GithubConnection connection = new GithubConnection();
    connection.setId(UUID.randomUUID());

    when(githubConnectionRepository.findByUserId(userId))
        .thenReturn(Optional.of(connection));

    when(repoSnapshotRepository.findByGithubConnectionId(connection.getId()))
        .thenReturn(
            List.of(
                repo("a", 5, "Java"),
                repo("b", 2, "Java"),
                repo("c", 1, "TypeScript"),
                repo("d", 0, null)));

    var summary = githubSummaryService.summarize(userId).orElseThrow();

    assertThat(summary.repoCount()).isEqualTo(4);
    assertThat(summary.totalStars()).isEqualTo(8);
    assertThat(summary.topLanguages())
        .containsExactly("Java", "TypeScript");
  }

  private RepoSnapshot repo(String name, int stars, String language) {
    RepoSnapshot repo = new RepoSnapshot();
    repo.setRepoName(name);
    repo.setStars(stars);
    repo.setPrimaryLanguage(language);
    return repo;
  }
}