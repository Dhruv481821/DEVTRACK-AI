package com.devtrack.ai.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * devtrack.ollama.* — only bound when devtrack.ai.provider=ollama (OllamaAiProvider).
 *
 * @param contextWindow Ollama's num_ctx. Its default is too small for a resume plus a job
 *     description plus the system instruction, which would silently truncate the prompt.
 * @param timeout local models on CPU can take tens of seconds per answer.
 */
@ConfigurationProperties(prefix = "devtrack.ollama")
@Validated
public record OllamaProperties(
    @NotBlank String baseUrl,
    @NotBlank String model,
    int contextWindow,
    @NotNull Duration timeout) {}
