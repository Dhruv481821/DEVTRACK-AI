package com.devtrack.github.service;

/**
 * FR-GH-02. GithubClient's own contract type — not a REST request/response DTO, so it lives
 * alongside the interface rather than in dto/, same placement as AiRequest/AiResponse next to
 * AiProvider.
 */
public record GithubRepoSummary(
    String repoName, int stars, String primaryLanguage, int commitsLast90Days) {}
