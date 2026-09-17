package com.devtrack.resume.dto.request;

import java.util.Map;

/**
 * All fields optional — PATCH semantics. sectionType is deliberately not editable — create a new
 * section instead of retyping an existing one.
 */
public record UpdateResumeSectionRequest(Map<String, Object> content, Integer orderIndex) {}
