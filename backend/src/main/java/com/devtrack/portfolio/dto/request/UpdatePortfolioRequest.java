package com.devtrack.portfolio.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdatePortfolioRequest(
    @Pattern(
        regexp = "^[a-z0-9][a-z0-9-]{1,28}[a-z0-9]$",
        message = "Use 3-30 lowercase letters, numbers or hyphens.")
    String slug,

    @Size(max = 160)
    String headline,

    Boolean published
) {}