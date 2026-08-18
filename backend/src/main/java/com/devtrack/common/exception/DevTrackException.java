package com.devtrack.common.exception;

/** Base for every business exception in the system — see /docs/07_Backend_Architecture.md §4. */
public abstract class DevTrackException extends RuntimeException {

  private final String errorCode;

  protected DevTrackException(String errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public String getErrorCode() {
    return errorCode;
  }
}
