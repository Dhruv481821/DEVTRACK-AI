package com.devtrack.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * FR-AUTH-01 — server-side validation is the real boundary (NFR-SEC-01), the frontend's Zod schema
 * mirrors this but never replaces it.
 */
public record RegisterRequest(
    @NotBlank @Email String email,
    @NotBlank
        @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d).{8,}$",
            message =
                "Password must be at least 8 characters and contain an uppercase letter and a number.")
        String password) {}
