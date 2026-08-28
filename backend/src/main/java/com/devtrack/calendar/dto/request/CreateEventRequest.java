package com.devtrack.calendar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/**
 * FR-CAL-01 — always creates a MANUAL event; there's no client-facing way to set source directly.
 */
public record CreateEventRequest(
    @NotBlank @Size(max = 200) String title,
    @Size(max = 2000) String description,
    @NotNull Instant startAt,
    Instant endAt) {}
