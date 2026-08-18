package com.devtrack.auth.dto.response;

/**
 * Refresh token is never in a response body — it's set as an httpOnly cookie by the controller. See
 * 06_API_Specification.md §2.1.
 */
public record AuthResponse(String accessToken) {

  /**
   * Service-layer-only carrier: pairs the client-facing AuthResponse with the raw refresh token
   * value, which the controller reads once to set the httpOnly cookie and then discards — it never
   * gets serialized into a JSON response.
   */
  public record WithRefreshToken(AuthResponse body, String rawRefreshToken) {}
}
