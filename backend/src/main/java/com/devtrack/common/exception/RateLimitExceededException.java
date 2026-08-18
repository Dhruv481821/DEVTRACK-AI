package com.devtrack.common.exception;

/** 429 — general Bucket4j rate limiting, distinct from AI-quota limiting. See 07_Backend_Architecture.md §6. */
public class RateLimitExceededException extends DevTrackException {
    public RateLimitExceededException(String message) {
        super("RATE_LIMITED", message);
    }
}
