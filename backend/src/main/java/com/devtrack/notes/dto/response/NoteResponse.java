package com.devtrack.notes.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record NoteResponse(
    UUID id,
    String title,
    Map<String, Object> content,
    List<String> tags,
    Instant createdAt,
    Instant updatedAt) {}
