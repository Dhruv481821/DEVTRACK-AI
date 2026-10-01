package com.devtrack.github.service;

import com.devtrack.common.security.TokenEncryptionService;
import com.devtrack.github.entity.GithubConnection;
import com.devtrack.github.entity.RepoSnapshot;
import com.devtrack.github.repository.GithubConnectionRepository;
import com.devtrack.github.repository.RepoSnapshotRepository;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * FR-GH-02. Syncs one connected GitHub account's repo data into RepoSnapshot and stamps
 * GithubConnection.lastSyncedAt. Called by GithubSyncScheduler on a schedule — never on a request
 * thread, per the PRD §8 risk this requirement exists to manage (don't hammer GitHub's API on every
 * dashboard load).
 */
@Service
public class GithubSyncService {

  private static final Logger log = LoggerFactory.getLogger(GithubSyncService.class);

  private final GithubConnectionRepository githubConnectionRepository;
  private final RepoSnapshotRepository repoSnapshotRepository;
  private final TokenEncryptionService tokenEncryptionService;
  private final GithubClient githubClient;

  public GithubSyncService(
      GithubConnectionRepository githubConnectionRepository,
      RepoSnapshotRepository repoSnapshotRepository,
      TokenEncryptionService tokenEncryptionService,
      GithubClient githubClient) {
    this.githubConnectionRepository = githubConnectionRepository;
    this.repoSnapshotRepository = repoSnapshotRepository;
    this.tokenEncryptionService = tokenEncryptionService;
    this.githubClient = githubClient;
  }

  /**
   * Syncs every connected account. One connection's failure (revoked token, GitHub outage, rate
   * limit) is logged and skipped rather than aborting the whole run — per NFR-REL-02, one user's
   * GitHub problem must never block another user's sync.
   */
  public void syncAllConnections() {
    List<GithubConnection> connections = githubConnectionRepository.findAll();

    for (GithubConnection connection : connections) {
      try {
        syncConnection(connection);
      } catch (Exception e) {
        log.warn(
            "GitHub sync failed for connection {} (user {}): {}",
            connection.getId(),
            connection.getUserId(),
            e.getMessage());
      }
    }
  }

  @Transactional
  public void syncConnection(GithubConnection connection) {
    String accessToken = tokenEncryptionService.decrypt(connection.getAccessTokenEncrypted());
    List<GithubRepoSummary> summaries = githubClient.fetchRepoSummaries(accessToken);
    Instant now = Instant.now();

    for (GithubRepoSummary summary : summaries) {
      RepoSnapshot snapshot =
          repoSnapshotRepository
              .findByGithubConnectionIdAndRepoName(connection.getId(), summary.repoName())
              .orElseGet(RepoSnapshot::new);

      snapshot.setGithubConnection(connection);
      snapshot.setRepoName(summary.repoName());
      snapshot.setStars(summary.stars());
      snapshot.setPrimaryLanguage(summary.primaryLanguage());
      snapshot.setCommitsLast90Days(summary.commitsLast90Days());
      snapshot.setSyncedAt(now);

      repoSnapshotRepository.save(snapshot);
    }

    connection.setLastSyncedAt(now);
    githubConnectionRepository.save(connection);
  }
}
