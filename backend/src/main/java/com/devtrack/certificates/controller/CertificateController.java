package com.devtrack.certificates.controller;

import com.devtrack.certificates.dto.request.CreateCertificateRequest;
import com.devtrack.certificates.dto.request.UpdateCertificateRequest;
import com.devtrack.certificates.dto.response.CertificateResponse;
import com.devtrack.certificates.service.CertificateService;
import com.devtrack.common.dto.ApiEnvelope;
import com.devtrack.common.security.CurrentUserResolver;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/certificates")
public class CertificateController {

  private final CertificateService certificateService;
  private final CurrentUserResolver currentUserResolver;

  public CertificateController(
      CertificateService certificateService, CurrentUserResolver currentUserResolver) {
    this.certificateService = certificateService;
    this.currentUserResolver = currentUserResolver;
  }

  @GetMapping
  public ApiEnvelope<List<CertificateResponse>> list() {
    return ApiEnvelope.success(
        certificateService.listMyCertificates(currentUserResolver.getCurrentUserId()));
  }

  @PostMapping
  public ApiEnvelope<CertificateResponse> create(
      @Valid @RequestBody CreateCertificateRequest request) {
    return ApiEnvelope.success(
        certificateService.create(currentUserResolver.getCurrentUserId(), request));
  }

  @PatchMapping("/{id}")
  public ApiEnvelope<CertificateResponse> update(
      @PathVariable UUID id, @Valid @RequestBody UpdateCertificateRequest request) {
    return ApiEnvelope.success(
        certificateService.update(id, currentUserResolver.getCurrentUserId(), request));
  }

  @DeleteMapping("/{id}")
  public ApiEnvelope<Void> delete(@PathVariable UUID id) {
    certificateService.delete(id, currentUserResolver.getCurrentUserId());
    return ApiEnvelope.success(null);
  }
}
