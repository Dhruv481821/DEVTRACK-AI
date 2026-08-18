package com.devtrack.common.exception;

/** 401 — bad credentials, expired/invalid token. */
public class AuthenticationException extends DevTrackException {
  public AuthenticationException(String errorCode, String message) {
    super(errorCode, message);
  }
}
