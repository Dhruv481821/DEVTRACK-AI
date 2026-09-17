package com.devtrack.github.controller;

import com.devtrack.common.dto.ApiEnvelope;
import com.devtrack.common.email.EmailProperties;
import com.devtrack.common.security.CurrentUserResolver;
import com.devtrack.github.dto.response.GithubConnectionResponse;
import com.devtrack.github.repository.GithubConnectionRepository;
import com.devtrack.github.service.GithubOAuthService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * FR-GH-01. /connection is authenticated; /callback is deliberately NOT (see SecurityConfig — must
 * be added to the permitAll list) since GitHub redirects the browser here with no DevTrack session
 * attached, per GithubOAuthService's docblock.
 */
@RestController
@RequestMapping("/api/v1/github")
public class GithubController {

  private final GithubOAuthService githubOAuthService;
  private final GithubConnectionRepository githubConnectionRepository;
  private final CurrentUserResolver currentUserResolver;
  private final EmailProperties emailProperties;

  public GithubController(
      GithubOAuthService githubOAuthService,
      GithubConnectionRepository githubConnectionRepository,
      CurrentUserResolver currentUserResolver,
      EmailProperties emailProperties) {
    this.githubOAuthService = githubOAuthService;
    this.githubConnectionRepository = githubConnectionRepository;
    this.currentUserResolver = currentUserResolver;
    this.emailProperties = emailProperties;
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
