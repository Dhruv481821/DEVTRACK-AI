package com.devtrack.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordResetConfirmRequest(
    @NotBlank String token,
    @NotBlank
        @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d).{8,}$",
            message =
                "Password must be at least 8 characters and contain an uppercase letter and a number.")
        String newPassword) {}
