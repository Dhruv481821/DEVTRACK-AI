package com.devtrack.resume.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateResumeRequest(
        @Size(max = 200) String title) {}