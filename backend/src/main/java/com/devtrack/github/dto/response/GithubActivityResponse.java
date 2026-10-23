package com.devtrack.github.dto.response;

/**
 * Activity aggregates for other modules (readiness analytics). Like GithubSummaryResponse it
 * deliberately carries no repo names.
 */
public record GithubActivityResponse(
    int repoCount, int commitsLast90Days, int activeRepos, int languageCount) {}
