package com.devtrack.profile.dto.request;

import jakarta.validation.constraints.Pattern;
import java.util.Map;

/** FR-SET-01. */
public record UpdateSettingsRequest(
        @Pattern(regexp = "^(dark|light)$", message = "Theme must be 'dark' or 'light'") String theme,
        Map<String, Object> notificationPrefs) {}
