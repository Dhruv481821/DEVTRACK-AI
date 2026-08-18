package com.devtrack.common.exception;

/** 409 — e.g. duplicate email on registration (FR-AUTH-01). */
public class ConflictException extends DevTrackException {
    public ConflictException(String message) {
        super("CONFLICT", message);
    }
}
