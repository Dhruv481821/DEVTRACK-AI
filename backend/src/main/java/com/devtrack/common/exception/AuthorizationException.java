package com.devtrack.common.exception;

/**
 * 403 — reserved for genuine role/permission denial, never for ownership checks (see
 * ResourceNotFoundException).
 */
public class AuthorizationException extends DevTrackException {
  public AuthorizationException(String message) {
    super("FORBIDDEN", message);
  }
}
