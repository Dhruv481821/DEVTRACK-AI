package com.devtrack.certificates.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devtrack.certificates.dto.request.CreateCertificateRequest;
import com.devtrack.certificates.dto.request.UpdateCertificateRequest;
import com.devtrack.certificates.entity.Certificate;
import com.devtrack.certificates.repository.CertificateRepository;
import com.devtrack.common.exception.ResourceNotFoundException;
import com.devtrack.common.security.OwnershipGuard;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CertificateServiceTest {

  private CertificateRepository certificateRepository;
  private CertificateService certificateService;

  private final UUID ownerId = UUID.randomUUID();
  private final UUID otherUserId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    certificateRepository = mock(CertificateRepository.class);

    certificateService = new CertificateService(certificateRepository, new OwnershipGuard());

    when(certificateRepository.save(any(Certificate.class))).thenAnswer(inv -> inv.getArgument(0));
  }

  @Test
  void create_savesWithCorrectOwner() {

    var response =
        certificateService.create(
            ownerId,
            new CreateCertificateRequest(
                "AWS Certified", "Amazon", LocalDate.now(), "https://verify.example"));

    assertThat(response.name()).isEqualTo("AWS Certified");
  }

  @Test
  void update_forNonOwner_throwsNotFound() {

    Certificate cert = existingCert(ownerId);

    when(certificateRepository.findById(cert.getId())).thenReturn(Optional.of(cert));

    assertThatThrownBy(
            () ->
                certificateService.update(
                    cert.getId(), otherUserId, new UpdateCertificateRequest("x", null, null, null)))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(certificateRepository, never()).save(any());
  }

  @Test
  void delete_setsDeletedAtInsteadOfHardDeleting() {

    Certificate cert = existingCert(ownerId);

    when(certificateRepository.findById(cert.getId())).thenReturn(Optional.of(cert));

    certificateService.delete(cert.getId(), ownerId);

    assertThat(cert.getDeletedAt()).isNotNull();

    verify(certificateRepository, never()).deleteById(any());
  }

  @Test
  void delete_forNonOwner_throwsNotFoundAndDoesNotDelete() {

    Certificate cert = existingCert(ownerId);

    when(certificateRepository.findById(cert.getId())).thenReturn(Optional.of(cert));

    assertThatThrownBy(() -> certificateService.delete(cert.getId(), otherUserId))
        .isInstanceOf(ResourceNotFoundException.class);

    assertThat(cert.getDeletedAt()).isNull();
  }

  private Certificate existingCert(UUID ownerId) {

    Certificate cert = new Certificate();

    cert.setId(UUID.randomUUID());
    cert.setUserId(ownerId);
    cert.setName("A cert");
    cert.setIssuingOrg("An org");

    return cert;
  }
}
