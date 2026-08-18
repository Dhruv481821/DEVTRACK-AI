package com.devtrack.auth.controller;

import com.devtrack.auth.dto.request.LoginRequest;
import com.devtrack.auth.dto.request.PasswordResetConfirmRequest;
import com.devtrack.auth.dto.request.PasswordResetRequestRequest;
import com.devtrack.auth.dto.request.RegisterRequest;
import com.devtrack.auth.dto.request.VerifyEmailRequest;
import com.devtrack.auth.dto.response.AuthResponse;
import com.devtrack.auth.service.AuthService;
import com.devtrack.common.dto.ApiEnvelope;
import com.devtrack.common.exception.AuthenticationException;
import com.devtrack.common.exception.RateLimitExceededException;
import com.devtrack.common.security.RateLimiterService;
import com.devtrack.common.security.RefreshCookieFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Implements FR-AUTH-01..05 endpoints per /docs/06_API_Specification.md §2.1. */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final String REFRESH_COOKIE_NAME = "refreshToken";

    private final AuthService authService;
    private final RateLimiterService rateLimiterService;
    private final RefreshCookieFactory refreshCookieFactory;

    public AuthController(
            AuthService authService, RateLimiterService rateLimiterService, RefreshCookieFactory refreshCookieFactory) {
        this.authService = authService;
        this.rateLimiterService = rateLimiterService;
        this.refreshCookieFactory = refreshCookieFactory;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiEnvelope<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(201).body(ApiEnvelope.success(null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiEnvelope<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        enforceAuthRateLimit(httpRequest, request.email());
        var result = authService.login(request);
        return withRefreshCookie(result);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiEnvelope<AuthResponse>> refresh(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken) {
        if (refreshToken == null) {
            throw new AuthenticationException("AUTH_TOKEN_EXPIRED", "No refresh token present.");
        }
        var result = authService.refresh(refreshToken);
        return withRefreshCookie(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiEnvelope<Void>> logout(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken) {
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }
        ResponseCookie clearCookie =
                refreshCookieFactory.build("", 0); // FR-AUTH-04 — clears the cookie client-side too
        return ResponseEntity.ok().header("Set-Cookie", clearCookie.toString()).body(ApiEnvelope.success(null));
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<ApiEnvelope<Void>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequestRequest request, HttpServletRequest httpRequest) {
        enforceAuthRateLimit(httpRequest, request.email());
        authService.requestPasswordReset(request.email());
        // Always 200, regardless of whether the email exists — AuthService's own
        // docblock explains why (never reveal account existence via this endpoint).
        return ResponseEntity.ok(ApiEnvelope.success(null));
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<ApiEnvelope<Void>> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.confirmPasswordReset(request);
        return ResponseEntity.ok(ApiEnvelope.success(null));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiEnvelope<Void>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request.token());
        return ResponseEntity.ok(ApiEnvelope.success(null));
    }

    // --- helpers ----------------------------------------------------------

    private ResponseEntity<ApiEnvelope<AuthResponse>> withRefreshCookie(
            AuthResponse.WithRefreshToken result) {
        ResponseCookie cookie =
                refreshCookieFactory.build(result.rawRefreshToken(), RefreshCookieFactory.MAX_AGE_SECONDS);
        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body(ApiEnvelope.success(result.body()));
    }

    /** NFR-SEC-02 — keyed by client IP + attempted email, so it limits both a single attacker and a single targeted account. */
    private void enforceAuthRateLimit(HttpServletRequest request, String email) {
        String key = request.getRemoteAddr() + ":" + email;
        if (!rateLimiterService.tryConsumeAuthAttempt(key)) {
            throw new RateLimitExceededException("Too many attempts. Please try again in a minute.");
        }
    }
}
