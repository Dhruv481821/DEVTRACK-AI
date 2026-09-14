package com.devtrack.resume.dto.response;

import java.util.Map;
import java.util.UUID;

public record ResumeSectionResponse(
        UUID id,
        String sectionType,
        Map<String, Object> content,
        int orderIndex) {}