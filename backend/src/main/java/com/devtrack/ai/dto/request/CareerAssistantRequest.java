package com.devtrack.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CareerAssistantRequest(
    @NotBlank(message = "Question is required")
        @Size(max = 1000, message = "Question must be 1,000 characters or fewer")
        String question) {}
