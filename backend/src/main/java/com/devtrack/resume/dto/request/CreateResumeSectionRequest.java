package com.devtrack.resume.dto.request;

import com.devtrack.resume.entity.SectionType;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record CreateResumeSectionRequest(
        @NotNull SectionType sectionType,
        Map<String, Object> content,
        int orderIndex) {}