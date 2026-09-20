package com.devtrack.ai.service;

import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Separate from GeminiProperties deliberately — this is a property of the RESUME_ATS agent (how
 * long its cached results stay valid), not of the Gemini provider itself. A future agent gets its
 * own such properties record rather than this one growing per-agent fields.
 */
@ConfigurationProperties(prefix = "devtrack.resume-ats")
@Validated
public record ResumeAtsProperties(@NotNull Duration cacheTtl) {}
