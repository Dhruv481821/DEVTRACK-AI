package com.devtrack.github.service;

import java.util.List;

/**
 * FR-GH-02. Abstracts GitHub's REST API the same way {@code AiProvider}/{@code EmailService}
 * abstract their external providers (07_Backend_Architecture.md §1) — {@code GithubSyncService}
 * depends on this interface, not on any HTTP detail, so tests substitute a fake per 13_Testing.md
 * §6 instead of hitting github.com.
 */
public interface GithubClient {

  /**
   * Fetches a summary of the authenticated user's repositories: name, star count, primary language,
   * and commits in roughly the last 90 days. Implementations decide how to source the 90-day figure
   * (GitHub has no single endpoint for it) — callers only see the result.
   */
  List<GithubRepoSummary> fetchRepoSummaries(String accessToken);
}
