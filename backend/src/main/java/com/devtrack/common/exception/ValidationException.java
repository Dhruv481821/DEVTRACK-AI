package com.devtrack.common.exception;

/** 400 — for validation failures not already caught by Bean Validation annotations. */
public class ValidationException extends DevTrackException {
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
    }
}
