package com.devtrack.certificates.dto.request;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdateCertificateRequest(
        @Size(max = 200) String name,
        @Size(max = 200) String issuingOrg,
        LocalDate issueDate,
        @Size(max = 500) String verificationUrl) {}