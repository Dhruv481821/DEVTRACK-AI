package com.devtrack.profile.dto.response;

import java.util.Map;

public record SettingsResponse(String theme, Map<String, Object> notificationPrefs) {}
