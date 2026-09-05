package com.devtrack.certificates.service;

import com.devtrack.certificates.dto.request.CreateCertificateRequest;
import com.devtrack.certificates.dto.request.UpdateCertificateRequest;
import com.devtrack.certificates.dto.response.CertificateResponse;
import com.devtrack.certificates.entity.Certificate;
import com.devtrack.certificates.repository.CertificateRepository;
import com.devtrack.common.exception.ResourceNotFoundException;
import com.devtrack.common.security.OwnershipGuard;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * FR-CERT-01.
 * Same CRUD + ownership + soft-delete pattern as Notes/Calendar/Study Planner.
 */
@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final OwnershipGuard ownershipGuard;

    public CertificateService(
            CertificateRepository certificateRepository,
            OwnershipGuard ownershipGuard) {
        this.certificateRepository = certificateRepository;
        this.ownershipGuard = ownershipGuard;
    }

    @Transactional(readOnly = true)
    public List<CertificateResponse> listMyCertificates(UUID userId) {
        return certificateRepository
                .findByUserIdOrderByIssueDateDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CertificateResponse create(
            UUID userId,
            CreateCertificateRequest request) {

        Certificate cert = new Certificate();

        cert.setUserId(userId);
        cert.setName(request.name());
        cert.setIssuingOrg(request.issuingOrg());
        cert.setIssueDate(request.issueDate());
        cert.setVerificationUrl(request.verificationUrl());

        Instant now = Instant.now();
        cert.setCreatedAt(now);
        cert.setUpdatedAt(now);

        certificateRepository.save(cert);

        return toResponse(cert);
    }

    @Transactional
    public CertificateResponse update(
            UUID certId,
            UUID userId,
            UpdateCertificateRequest request) {

        Certificate cert = findOrThrow(certId);

        ownershipGuard.assertOwnedBy(cert.getUserId(), userId);

        if (request.name() != null) {
            cert.setName(request.name());
        }

        if (request.issuingOrg() != null) {
            cert.setIssuingOrg(request.issuingOrg());
        }

        if (request.issueDate() != null) {
            cert.setIssueDate(request.issueDate());
        }

        if (request.verificationUrl() != null) {
            cert.setVerificationUrl(request.verificationUrl());
        }

        cert.setUpdatedAt(Instant.now());

        certificateRepository.save(cert);

        return toResponse(cert);
    }

    @Transactional
    public void delete(UUID certId, UUID userId) {

        Certificate cert = findOrThrow(certId);

        ownershipGuard.assertOwnedBy(cert.getUserId(), userId);

        cert.setDeletedAt(Instant.now());

        certificateRepository.save(cert);
    }

    private Certificate findOrThrow(UUID certId) {
        return certificateRepository
                .findById(certId)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Certificate not found."));
    }

    private CertificateResponse toResponse(Certificate cert) {
        return new CertificateResponse(
                cert.getId(),
                cert.getName(),
                cert.getIssuingOrg(),
                cert.getIssueDate(),
                cert.getVerificationUrl());
    }
}