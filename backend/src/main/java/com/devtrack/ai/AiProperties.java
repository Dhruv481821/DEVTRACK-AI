package com.devtrack.ai;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * devtrack.ai.* — the 09_AI_Architecture.md §7 per-user daily cap, split by billing tier. One
 * budget per user per day shared across every AI feature (ATS scoring and the career assistant).
 */
@ConfigurationProperties(prefix = "devtrack.ai")
@Validated
public record AiProperties(@Min(1) int freeDailyRequestLimit, @Min(1) int proDailyRequestLimit) {}
