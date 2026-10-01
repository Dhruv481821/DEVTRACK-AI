package com.devtrack.github.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devtrack.common.security.TokenEncryptionService;
import com.devtrack.github.entity.GithubConnection;
import com.devtrack.github.entity.RepoSnapshot;
import com.devtrack.github.repository.GithubConnectionRepository;
import com.devtrack.github.repository.RepoSnapshotRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GithubSyncServiceTest {

  private GithubConnectionRepository githubConnectionRepository;
  private RepoSnapshotRepository repoSnapshotRepository;
  private TokenEncryptionService tokenEncryptionService;
  private GithubClient githubClient;
  private GithubSyncService githubSyncService;

  @BeforeEach
  void setUp() {
    githubConnectionRepository = mock(GithubConnectionRepository.class);
    repoSnapshotRepository = mock(RepoSnapshotRepository.class);
    tokenEncryptionService = mock(TokenEncryptionService.class);
    githubClient = mock(GithubClient.class);

    githubSyncService =
        new GithubSyncService(
            githubConnectionRepository,
            repoSnapshotRepository,
            tokenEncryptionService,
            githubClient);

    when(repoSnapshotRepository.save(any(RepoSnapshot.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    when(githubConnectionRepository.save(any(GithubConnection.class)))
        .thenAnswer(inv -> inv.getArgument(0));
  }

  private GithubConnection aConnection(String encryptedToken) {
    GithubConnection connection = new GithubConnection();
    connection.setId(UUID.randomUUID());
    connection.setUserId(UUID.randomUUID());
    connection.setGithubUsername("octocat");
    connection.setAccessTokenEncrypted(encryptedToken);
    return connection;
  }

  @Test
  void syncConnection_createsNewSnapshotWhenNoneExists() {
    GithubConnection connection = aConnection("encrypted-token");

    when(tokenEncryptionService.decrypt("encrypted-token")).thenReturn("plain-token");

    when(githubClient.fetchRepoSummaries("plain-token"))
        .thenReturn(List.of(new GithubRepoSummary("devtrack-ai", 12, "Java", 34)));

    when(repoSnapshotRepository.findByGithubConnectionIdAndRepoName(
            connection.getId(), "devtrack-ai"))
        .thenReturn(Optional.empty());

    githubSyncService.syncConnection(connection);

    verify(repoSnapshotRepository)
        .save(
            argThat(
                snapshot ->
                    snapshot.getRepoName().equals("devtrack-ai")
                        && snapshot.getStars() == 12
                        && "Java".equals(snapshot.getPrimaryLanguage())
                        && snapshot.getCommitsLast90Days() == 34));

    assertThat(connection.getLastSyncedAt()).isNotNull();
  }

  @Test
  void syncConnection_updatesExistingSnapshotInsteadOfDuplicating() {
    GithubConnection connection = aConnection("encrypted-token");

    RepoSnapshot existing = new RepoSnapshot();
    existing.setId(UUID.randomUUID());
    existing.setRepoName("devtrack-ai");
    existing.setStars(1);

    when(tokenEncryptionService.decrypt("encrypted-token")).thenReturn("plain-token");

    when(githubClient.fetchRepoSummaries("plain-token"))
        .thenReturn(List.of(new GithubRepoSummary("devtrack-ai", 99, "Java", 5)));

    when(repoSnapshotRepository.findByGithubConnectionIdAndRepoName(
            connection.getId(), "devtrack-ai"))
        .thenReturn(Optional.of(existing));

    githubSyncService.syncConnection(connection);

    assertThat(existing.getStars()).isEqualTo(99);

    verify(repoSnapshotRepository, times(1)).save(existing);
  }

  @Test
  void syncAllConnections_oneConnectionFailing_doesNotBlockOthers() {
    GithubConnection failing = aConnection("failing-encrypted-token");

    GithubConnection healthy = aConnection("healthy-encrypted-token");

    when(githubConnectionRepository.findAll()).thenReturn(List.of(failing, healthy));

    when(tokenEncryptionService.decrypt("failing-encrypted-token"))
        .thenThrow(new IllegalStateException("bad token"));

    when(tokenEncryptionService.decrypt("healthy-encrypted-token")).thenReturn("plain-token");

    when(githubClient.fetchRepoSummaries("plain-token")).thenReturn(List.of());

    githubSyncService.syncAllConnections();

    assertThat(healthy.getLastSyncedAt()).isNotNull();
    assertThat(failing.getLastSyncedAt()).isNull();

    verify(githubConnectionRepository, never()).save(failing);
  }
}
