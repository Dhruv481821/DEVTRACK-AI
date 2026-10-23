package com.devtrack.ai.controller;

import com.devtrack.ai.dto.response.AiQuotaResponse;
import com.devtrack.ai.service.AiQuotaService;
import com.devtrack.common.dto.ApiEnvelope;
import com.devtrack.common.security.CurrentUserResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai/quota")
public class AiQuotaController {

  private final AiQuotaService aiQuotaService;
  private final CurrentUserResolver currentUserResolver;

  public AiQuotaController(AiQuotaService aiQuotaService, CurrentUserResolver currentUserResolver) {
    this.aiQuotaService = aiQuotaService;
    this.currentUserResolver = currentUserResolver;
  }

  @GetMapping
  public ApiEnvelope<AiQuotaResponse> get() {
    return ApiEnvelope.success(aiQuotaService.describe(currentUserResolver.getCurrentUserId()));
  }
}
