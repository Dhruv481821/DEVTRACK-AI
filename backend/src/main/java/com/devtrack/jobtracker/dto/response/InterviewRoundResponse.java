package com.devtrack.jobtracker.dto.response;

import com.devtrack.jobtracker.entity.InterviewOutcome;
import java.time.Instant;
import java.util.UUID;

public record InterviewRoundResponse(
    UUID id,
    String roundName,
    Instant occurredAt,
    InterviewOutcome outcome,
    String notes,
    Instant createdAt,
    Instant updatedAt) {}
