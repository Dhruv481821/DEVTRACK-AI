package com.devtrack.calendar.dto.request;

import jakarta.validation.constraints.Size;
import java.time.Instant;

/**
 * All fields optional — PATCH semantics. Rejected entirely for non-MANUAL events — see
 * EventService.
 */
public record UpdateEventRequest(
    @Size(max = 200) String title,
    @Size(max = 2000) String description,
    Instant startAt,
    Instant endAt) {}
