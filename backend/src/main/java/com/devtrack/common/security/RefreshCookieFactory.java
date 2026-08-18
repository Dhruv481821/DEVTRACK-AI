package com.devtrack.common.security;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * httpOnly, secure, SameSite=Strict — the transport decision from
 * 06_API_Specification.md §2.1 and the CSRF reasoning in 12_Security.md §11.
 * Extracted here because both AuthController (password login) and
 * OAuth2LoginSuccessHandler (Google login) need to build the exact same cookie —
 * duplicating this between them would be a real DRY violation for something this
 * security-sensitive, where the two copies drifting apart is a real risk, not a
 * cosmetic one.
 */
@Component
public class RefreshCookieFactory {

    private static final String COOKIE_NAME = "refreshToken";
    public static final int MAX_AGE_SECONDS = 30 * 24 * 60 * 60; // 30 days — matches AuthService's REFRESH_TOKEN_TTL

    public ResponseCookie build(String value, int maxAgeSeconds) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(maxAgeSeconds)
                .build();
    }
}
