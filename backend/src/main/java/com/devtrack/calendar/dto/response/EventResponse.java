package com.devtrack.calendar.dto.response;

import java.time.Instant;
import java.util.UUID;

public record EventResponse(
    UUID id, String title, String description, Instant startAt, Instant endAt, String source) {}
