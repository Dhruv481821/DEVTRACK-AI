package com.devtrack.github.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * FR-GH-02 — "sync on a schedule, not on every page load." Hourly, matching the 1-hour GitHub
 * sync-snapshot cache TTL decided in 05_Database_Architecture.md §3. Safe only because the API runs
 * as a single Railway instance (04_System_Architecture.md §8) — Spring's {@code @Scheduled} would
 * double-fire this across multiple instances; that constraint is accepted there, not solved here.
 *
 * <p>Update: the "wait up to an hour for a first sync" gap noted when this class was first added is
 * closed — GithubController#callback now triggers an immediate synchronous sync right after
 * connecting. This scheduler still owns every subsequent (re)sync; it's just no longer the only
 * path to a connected user's first one.
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
