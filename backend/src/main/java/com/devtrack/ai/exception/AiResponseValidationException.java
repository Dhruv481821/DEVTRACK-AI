package com.devtrack.ai.exception;

/**
 * Thrown internally within com.devtrack.ai when a provider call succeeded (AiResponse.success() ==
 * true) but its jsonPayload doesn't deserialize into the expected result type — e.g. Gemini
 * returned something that doesn't match the requested schema despite the schema constraint. Never
 * reaches GlobalExceptionHandler or a controller: ResumeAtsService catches this and treats it
 * exactly like a provider failure (falls through to the cached-fallback/unavailable path in
 * /docs/09_AI_Architecture.md §8), since from the caller's perspective "the model returned garbage"
 * and "the model didn't respond" need the same degraded-mode handling.
 */
public class AiResponseValidationException extends RuntimeException {

  public AiResponseValidationException(String message, Throwable cause) {
    super(message, cause);
  }
}
