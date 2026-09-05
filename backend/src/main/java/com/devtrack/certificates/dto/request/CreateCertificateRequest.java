package com.devtrack.certificates.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * FR-CERT-01.
 * verificationUrl is stored as-is and never validated by fetching it.
 */
public record CreateCertificateRequest(
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 200) String issuingOrg,
        LocalDate issueDate,
        @Size(max = 500) String verificationUrl) {}