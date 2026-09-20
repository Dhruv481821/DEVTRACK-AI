package com.devtrack.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AtsScoreRequest(
    @NotBlank(message = "Job description is required")
        @Size(max = 20000, message = "Job description must be 20,000 characters or fewer")
        String jobDescription) {}
