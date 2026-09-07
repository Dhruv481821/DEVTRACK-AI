package com.devtrack.certificates.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record CertificateResponse(
    UUID id, String name, String issuingOrg, LocalDate issueDate, String verificationUrl) {}
