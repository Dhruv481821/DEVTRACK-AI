package com.devtrack.profile.dto.request;

import jakarta.validation.constraints.Size;

/** FR-PROF-01. Both fields optional — a PATCH updates only what's provided. */
public record UpdateProfileRequest(
        @Size(max = 100, message = "Display name must be 100 characters or fewer") String displayName,
        @Size(max = 500, message = "Bio must be 500 characters or fewer") String bio) {}
