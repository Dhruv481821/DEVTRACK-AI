package com.devtrack.github.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * FR-GH-02 — "sync on a schedule, not on every page load." Hourly, matching the 1-hour GitHub
 * sync-snapshot cache TTL decided in 05_Database_Architecture.md §3. Safe only because the API runs
 * as a single Railway instance (04_System_Architecture.md §8) — Spring's {@code @Scheduled} would
 * double-fire this across multiple instances; that constraint is accepted there, not solved here.
 *
 * <p>Known gap, not solved in this batch: a user who just connected GitHub waits up to one hour for
 * their first sync rather than getting one immediately on connect. Revisit if that's worth an
 * on-demand "sync now" trigger — not added here to keep this batch to the scheduled-sync
 * requirement as written.
 */
@Component
public class GithubSyncScheduler {

  private static final long ONE_HOUR_MS = 3_600_000L;

  private final GithubSyncService githubSyncService;

  public GithubSyncScheduler(GithubSyncService githubSyncService) {
    this.githubSyncService = githubSyncService;
  }

  @Scheduled(fixedRate = ONE_HOUR_MS, initialDelay = ONE_HOUR_MS)
  public void syncAllConnections() {
    githubSyncService.syncAllConnections();
  }
}
