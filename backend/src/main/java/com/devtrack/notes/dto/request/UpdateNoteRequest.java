package com.devtrack.notes.dto.request;

import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

/** All fields optional — PATCH semantics, same convention as UpdateProfileRequest. */
public record UpdateNoteRequest(
    @Size(max = 200, message = "Title must be 200 characters or fewer") String title,
    Map<String, Object> content,
    List<String> tagNames) {}
