package com.devtrack.github.dto.response;

import java.util.List;

/**
 * Aggregate-only view of a user's synced repos, for consumers outside the github module.
 * Deliberately contains no repo names, so private repository names are not exposed.
 */
public record GithubSummaryResponse(
    int repoCount,
    int totalStars,
    List<String> topLanguages
) {}