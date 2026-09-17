package com.devtrack.resume.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ResumeResponse(UUID id, String title, Instant createdAt, Instant updatedAt) {}
