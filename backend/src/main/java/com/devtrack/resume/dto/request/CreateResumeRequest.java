package com.devtrack.resume.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateResumeRequest(
        @NotBlank @Size(max = 200) String title) {}