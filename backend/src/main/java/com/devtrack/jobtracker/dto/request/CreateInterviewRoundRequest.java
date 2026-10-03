package com.devtrack.jobtracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

// outcome isn't settable on create — every new round starts PENDING
// (InterviewRound.outcome's own default), same reasoning as
// CreateJobApplicationRequest leaving stage out.
public record CreateInterviewRoundRequest(
    @NotBlank @Size(max = 200) String roundName, Instant occurredAt, String notes) {}
