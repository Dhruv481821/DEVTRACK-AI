package com.devtrack.notifications.dto.response;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
    UUID id, String type, String payload, boolean read, Instant createdAt) {}
