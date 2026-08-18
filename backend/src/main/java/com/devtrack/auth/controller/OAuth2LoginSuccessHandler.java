package com.devtrack.auth.controller;

import com.devtrack.auth.dto.response.AuthResponse;
import com.devtrack.auth.service.AuthService;
import com.devtrack.common.email.EmailProperties;
import com.devtrack.common.security.RefreshCookieFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * Bridges Spring Security's OAuth2 login flow into DevTrack's own token system — per FR-AUTH-02's
 * acceptance criterion, the frontend must receive the same shape of credentials (access token +
 * refresh cookie) regardless of how the user authenticated. This is the one place that difference
 * is bridged.
 *
 * <p>Since this is a browser redirect flow (not an API call the frontend awaits a JSON response
 * from), the access token is delivered via a redirect query parameter rather than a response body —
 * the frontend's not-yet-built `/oauth-callback` route (Week 3) is expected to read it from there
 * and store it in the Zustand auth store (08_Frontend_Architecture.md §4), exactly like the
 * password-login response does. The refresh token still never touches the URL or any JS-readable
 * location — it's set as the same httpOnly cookie either way.
 */
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

  private final AuthService authService;
  private final RefreshCookieFactory refreshCookieFactory;
  private final EmailProperties emailProperties;

  public OAuth2LoginSuccessHandler(
      AuthService authService,
      RefreshCookieFactory refreshCookieFactory,
      EmailProperties emailProperties) {
    this.authService = authService;
    this.refreshCookieFactory = refreshCookieFactory;
    this.emailProperties = emailProperties;
  }

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {
    OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
    String email = oauth2User.getAttribute("email");

    AuthResponse.WithRefreshToken result = authService.loginOrRegisterWithGoogle(email);

    ResponseCookie cookie =
        refreshCookieFactory.build(result.rawRefreshToken(), RefreshCookieFactory.MAX_AGE_SECONDS);
    response.addHeader("Set-Cookie", cookie.toString());

    String encodedToken = URLEncoder.encode(result.body().accessToken(), StandardCharsets.UTF_8);
    response.sendRedirect(
        emailProperties.frontend().baseUrl() + "/oauth-callback?token=" + encodedToken);
  }
}
