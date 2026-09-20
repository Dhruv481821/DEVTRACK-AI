package com.devtrack.ai;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * devtrack.ai.daily-request-limit already existed in application.yml before this feature was built
 * — 09_AI_Architecture.md §7 pre-planned it as a limit "shared across all agents." This record just
 * gives that existing key a typed home; the value itself (20) was not changed.
 */
@ConfigurationProperties(prefix = "devtrack.ai")
@Validated
public record AiProperties(@Min(1) int dailyRequestLimit) {}
