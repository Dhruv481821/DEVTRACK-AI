package com.devtrack.notes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

/**
 * FR-NOTES-01. content is Tiptap's JSON document shape — validated structurally by the editor, not
 * here.
 */
public record CreateNoteRequest(
    @NotBlank @Size(max = 200, message = "Title must be 200 characters or fewer") String title,
    Map<String, Object> content,
    List<String> tagNames) {}
