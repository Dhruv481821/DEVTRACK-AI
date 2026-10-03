package com.devtrack.github.controller;

import com.devtrack.common.dto.ApiEnvelope;
import com.devtrack.common.email.EmailProperties;
import com.devtrack.common.security.CurrentUserResolver;
import com.devtrack.github.dto.response.GithubConnectionResponse;
import com.devtrack.github.dto.response.RepoSnapshotResponse;
import com.devtrack.github.entity.GithubConnection;
import com.devtrack.github.repository.GithubConnectionRepository;
import com.devtrack.github.repository.RepoSnapshotRepository;
import com.devtrack.github.service.GithubOAuthService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * FR-GH-01/FR-GH-03. /connection and /repos are authenticated; /callback is deliberately NOT (see
 * SecurityConfig — must be added to the permitAll list) since GitHub redirects the browser here
 * with no DevTrack session attached, per GithubOAuthService's docblock.
 */
@RestController
@RequestMapping("/api/v1/github")
public class GithubController {

  private final GithubOAuthService githubOAuthService;
  private final GithubConnectionRepository githubConnectionRepository;
  private final RepoSnapshotRepository repoSnapshotRepository;
  private final CurrentUserResolver currentUserResolver;
  private final EmailProperties emailProperties;

  public GithubController(
      GithubOAuthService githubOAuthService,
      GithubConnectionRepository githubConnectionRepository,
      RepoSnapshotRepository repoSnapshotRepository,
      CurrentUserResolver currentUserResolver,
      EmailProperties emailProperties) {
    this.githubOAuthService = githubOAuthService;
    this.githubConnectionRepository = githubConnectionRepository;
    this.repoSnapshotRepository = repoSnapshotRepository;
    this.currentUserResolver = currentUserResolver;
    this.emailProperties = emailProperties;
  }

  /**
   * FR-GH-03. Repo-level data only (name, stars, language, commits-last-90-days) — the schema has
   * no day-level commit granularity (repo_snapshot stores one 90-day aggregate per repo, per
   * V8__github_analytics.sql), so a true day-by-day contribution heatmap isn't buildable from
   * current data without a schema change. Not invented here; the frontend derives a top-language
   * breakdown from this list client-side instead of a second backend aggregation endpoint.
   */
  @GetMapping("/repos")
  public ApiEnvelope<List<RepoSnapshotResponse>> repos() {
    UUID userId = currentUserResolver.getCurrentUserId();

    List<RepoSnapshotResponse> repos =
        githubConnectionRepository
            .findByUserId(userId)
            .map(GithubConnection::getId)
            .map(repoSnapshotRepository::findByGithubConnectionId)
            .orElseGet(List::of)
            .stream()
            .map(
                r ->
                    new RepoSnapshotResponse(
                        r.getId(),
                        r.getRepoName(),
                        r.getStars(),
                        r.getPrimaryLanguage(),
                        r.getCommitsLast90Days(),
                        r.getSyncedAt()))
            .sorted(Comparator.comparingInt(RepoSnapshotResponse::commitsLast90Days).reversed())
            .toList();

    return ApiEnvelope.success(repos);
  }

  @GetMapping("/connection")
  public ApiEnvelope<GithubConnectionResponse> connectionStatus() {
    return ApiEnvelope.success(
        githubConnectionRepository
            .findByUserId(currentUserResolver.getCurrentUserId())
            .map(
                c -> new GithubConnectionResponse(true, c.getGithubUsername(), c.getLastSyncedAt()))
            .orElseGet(GithubConnectionResponse::notConnected));
  }

  @GetMapping("/oauth-url")
  public ApiEnvelope<String> oauthUrl() {
    return ApiEnvelope.success(
        githubOAuthService.buildAuthorizationUrl(currentUserResolver.getCurrentUserId()));
  }

  @GetMapping("/callback")
  public void callback(
      @RequestParam String code, @RequestParam String state, HttpServletResponse response)
      throws IOException {

    githubOAuthService.handleCallback(code, state);

    // Redirect to the actual configured frontend after successful GitHub connection.
    response.sendRedirect(emailProperties.frontend().baseUrl() + "/settings?github=connected");
  }
}
