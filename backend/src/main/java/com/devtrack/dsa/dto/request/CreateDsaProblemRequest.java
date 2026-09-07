package com.devtrack.dsa.dto.request;

import com.devtrack.dsa.entity.Difficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/** FR-DSA-01. */
public record CreateDsaProblemRequest(
    @NotBlank @Size(max = 200) String title, @NotNull Difficulty difficulty, List<String> tags) {}
