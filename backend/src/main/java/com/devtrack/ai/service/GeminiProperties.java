package com.devtrack.ai.service;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Same shape/registration pattern as GithubOAuthProperties — registered
 * via @EnableConfigurationProperties on the class that consumes it (GeminiAiProvider), since this
 * project has no @ConfigurationPropertiesScan.
 *
 * <p>No daily-quota field here — that already existed as devtrack.ai.daily-request-limit in
 * application.yml before this feature was built (09_AI_Architecture.md §7's pre-planned value). See
 * AiProperties, which gives that existing key a typed home instead of duplicating it under a
 * Gemini-specific path.
 *
 * <p>model defaults to a value that WILL drift — Google has already shipped one breaking change to
 * this API's response shape and renamed the recommended Flash model multiple times within 2026.
 * Verify devtrack.gemini.model against Google's current model list at deploy time rather than
 * trusting this default indefinitely.
 */
@ConfigurationProperties(prefix = "devtrack.gemini")
@Validated
public record GeminiProperties(@NotBlank String apiKey, @NotBlank String model) {}
