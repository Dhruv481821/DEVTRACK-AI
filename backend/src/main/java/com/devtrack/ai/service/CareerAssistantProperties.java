package com.devtrack.ai.service;

import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Per-agent settings for CAREER_ASSISTANT — same split as ResumeAtsProperties. */
@ConfigurationProperties(prefix = "devtrack.career-assistant")
@Validated
public record CareerAssistantProperties(@NotNull Duration cacheTtl) {}
