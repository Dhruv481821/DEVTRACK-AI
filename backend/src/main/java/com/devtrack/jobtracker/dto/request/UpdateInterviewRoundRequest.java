package com.devtrack.jobtracker.dto.request;

import com.devtrack.jobtracker.entity.InterviewOutcome;
import jakarta.validation.constraints.Size;
import java.time.Instant;

// Same nullable-partial-update pattern as UpdateJobApplicationRequest — a null
// field means "leave unchanged."
public record UpdateInterviewRoundRequest(
    @Size(max = 200) String roundName,
    Instant occurredAt,
    InterviewOutcome outcome,
    String notes) {}
