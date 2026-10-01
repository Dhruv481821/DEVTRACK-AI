package com.devtrack.github.service;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * FR-GH-02. The only class that knows GitHub's REST API shape — same isolation principle as
 * GeminiAiProvider being the only class that knows Gemini's shape. RestClient built per-instance
 * from the injected Builder, same pattern as GithubOAuthService.
 *
 * <p>GitHub has no single endpoint for "commits in the last 90 days," so this calls each repo's
 * {@code stats/commit_activity} endpoint (52 weekly buckets) and sums the most recent 13. That
 * endpoint computes asynchronously on a repo's first request and returns 202 with no data while it
 * does — treated as 0 here rather than retried, consistent with NFR-REL-02 (a sync gap shows as 0
 * commits this run, not a broken page); a later scheduled run picks up the real count once GitHub
 * has finished computing it.
 *
 * <p>No pagination beyond the first 100 repos, no fork filtering — not required by FR-GH-02 as
 * written, and adding either now would be scope the requirement doesn't ask for.
 */
@Service
public class GithubApiClient implements GithubClient {

  private static final Logger log = LoggerFactory.getLogger(GithubApiClient.class);
  private static final String GITHUB_API_BASE = "https://api.github.com";
  private static final String ACCEPT_HEADER = "application/vnd.github+json";
  private static final int WEEKS_IN_90_DAYS = 13;

  private final RestClient restClient;

  public GithubApiClient(RestClient.Builder restClientBuilder) {
    this.restClient = restClientBuilder.build();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<GithubRepoSummary> fetchRepoSummaries(String accessToken) {
    List<Map<String, Object>> repos =
        restClient
            .get()
            .uri(GITHUB_API_BASE + "/user/repos?per_page=100&sort=pushed&affiliation=owner")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .header(HttpHeaders.ACCEPT, ACCEPT_HEADER)
            .retrieve()
            .body(List.class);

    if (repos == null) {
      return List.of();
    }

    return repos.stream().map(repo -> toSummary(repo, accessToken)).toList();
  }

  private GithubRepoSummary toSummary(Map<String, Object> repo, String accessToken) {
    String name = String.valueOf(repo.get("name"));
    String fullName = String.valueOf(repo.get("full_name"));
    int stars = ((Number) repo.getOrDefault("stargazers_count", 0)).intValue();
    Object language = repo.get("language");
    int commits = fetchCommitsLast90Days(fullName, accessToken);

    return new GithubRepoSummary(
        name, stars, language == null ? null : language.toString(), commits);
  }

  @SuppressWarnings("unchecked")
  private int fetchCommitsLast90Days(String fullName, String accessToken) {
    try {
      List<Map<String, Object>> weeks =
          restClient
              .get()
              .uri(GITHUB_API_BASE + "/repos/" + fullName + "/stats/commit_activity")
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
              .header(HttpHeaders.ACCEPT, ACCEPT_HEADER)
              .exchange(
                  (request, response) -> {
                    if (!response.getStatusCode().is2xxSuccessful()) {
                      return List.<Map<String, Object>>of();
                    }

                    return (List<Map<String, Object>>) response.bodyTo(List.class);
                  });

      if (weeks == null || weeks.isEmpty()) {
        return 0;
      }

      int fromIndex = Math.max(0, weeks.size() - WEEKS_IN_90_DAYS);
      return weeks.subList(fromIndex, weeks.size()).stream()
          .mapToInt(week -> ((Number) week.getOrDefault("total", 0)).intValue())
          .sum();
    } catch (RestClientException e) {
      log.warn("Failed to fetch commit activity for {}: {}", fullName, e.getMessage());
      return 0;
    }
  }
}
