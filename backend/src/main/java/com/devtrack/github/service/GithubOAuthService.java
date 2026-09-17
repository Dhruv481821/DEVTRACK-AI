package com.devtrack.github.service;

import com.devtrack.common.exception.AuthenticationException;
import com.devtrack.common.security.TokenEncryptionService;
import com.devtrack.github.entity.GithubConnection;
import com.devtrack.github.repository.GithubConnectionRepository;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

/**
 * FR-GH-01. Plain REST calls to GitHub's OAuth endpoints — NOT Spring Security's oauth2Login() —
 * per the architectural note in this slice: connecting a secondary account for an
 * already-authenticated user is a different problem than establishing identity via OAuth, and
 * forcing it into the login-flow abstraction would fight the framework rather than use it
 * naturally.
 *
 * <p>Read-only-intent scope ("read:user repo") per 04_System_Architecture.md §4's OAuth scope
 * decision — worth a final check against GitHub's current documented scope list at implementation
 * time, since GitHub's exact scope granularity for read-only repo access has changed across API
 * versions historically.
 */
@Service
@EnableConfigurationProperties(GithubOAuthProperties.class)
public class GithubOAuthService {

  private static final String AUTHORIZE_URL = "https://github.com/login/oauth/authorize";

  private static final String TOKEN_URL = "https://github.com/login/oauth/access_token";

  private static final String USER_API_URL = "https://api.github.com/user";

  private static final String SCOPE = "read:user repo";

  private final GithubOAuthProperties properties;
  private final GithubOAuthStateService stateService;
  private final GithubConnectionRepository githubConnectionRepository;
  private final TokenEncryptionService tokenEncryptionService;
  private final RestClient restClient;

  public GithubOAuthService(
      GithubOAuthProperties properties,
      GithubOAuthStateService stateService,
      GithubConnectionRepository githubConnectionRepository,
      TokenEncryptionService tokenEncryptionService,
      RestClient.Builder restClientBuilder) {
    this.properties = properties;
    this.stateService = stateService;
    this.githubConnectionRepository = githubConnectionRepository;
    this.tokenEncryptionService = tokenEncryptionService;

    // Spring Boot auto-configures a RestClient.Builder bean.
    // Using it here keeps the HTTP client testable and replaceable.
    this.restClient = restClientBuilder.build();
  }

  /**
   * Called by an authenticated endpoint — the state ties this specific user to the eventual
   * callback.
   */
  public String buildAuthorizationUrl(UUID userId) {
    String state = stateService.createState(userId);

    String encodedRedirect = URLEncoder.encode(properties.redirectUri(), StandardCharsets.UTF_8);

    return AUTHORIZE_URL
        + "?client_id="
        + properties.clientId()
        + "&redirect_uri="
        + encodedRedirect
        + "&scope="
        + URLEncoder.encode(SCOPE, StandardCharsets.UTF_8)
        + "&state="
        + state;
  }

  /**
   * Called by the unauthenticated callback endpoint — GitHub redirects the browser here with no
   * DevTrack session/JWT attached, so the state parameter (not the request's own auth) is what
   * identifies the user.
   */
  @Transactional
  public void handleCallback(String code, String state) {
    UUID userId =
        stateService
            .consumeState(state)
            .orElseThrow(
                () ->
                    new AuthenticationException(
                        "AUTH_INVALID_CREDENTIALS",
                        "Invalid or expired GitHub connection request."));

    String accessToken = exchangeCodeForToken(code);
    String githubUsername = fetchGithubUsername(accessToken);

    GithubConnection connection =
        githubConnectionRepository.findByUserId(userId).orElseGet(GithubConnection::new);

    connection.setUserId(userId);
    connection.setGithubUsername(githubUsername);
    connection.setAccessTokenEncrypted(tokenEncryptionService.encrypt(accessToken));

    if (connection.getConnectedAt() == null) {
      connection.setConnectedAt(Instant.now());
    }

    githubConnectionRepository.save(connection);
  }

  @SuppressWarnings("unchecked")
  private String exchangeCodeForToken(String code) {
    Map<String, Object> response =
        restClient
            .post()
            .uri(TOKEN_URL)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .body(
                Map.of(
                    "client_id",
                    properties.clientId(),
                    "client_secret",
                    properties.clientSecret(),
                    "code",
                    code,
                    "redirect_uri",
                    properties.redirectUri()))
            .retrieve()
            .body(Map.class);

    Object accessToken = response.get("access_token");

    if (accessToken == null) {
      throw new IllegalStateException("GitHub did not return an access_token: " + response);
    }

    return accessToken.toString();
  }

  @SuppressWarnings("unchecked")
  private String fetchGithubUsername(String accessToken) {
    Map<String, Object> response =
        restClient
            .get()
            .uri(USER_API_URL)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .retrieve()
            .body(Map.class);

    return String.valueOf(response.get("login"));
  }
}
