package com.devtrack.ai.controller;

import com.devtrack.ai.dto.request.CareerAssistantRequest;
import com.devtrack.ai.dto.response.CareerAssistantResponse;
import com.devtrack.ai.service.CareerAssistantService;
import com.devtrack.common.dto.ApiEnvelope;
import com.devtrack.common.security.CurrentUserResolver;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** FR-AI-01, per 06_API_Specification.md's Phase 5 resource root (/api/v1/ai/assistant). */
@RestController
@RequestMapping("/api/v1/ai/assistant")
public class CareerAssistantController {

  private final CareerAssistantService careerAssistantService;
  private final CurrentUserResolver currentUserResolver;

  public CareerAssistantController(
      CareerAssistantService careerAssistantService, CurrentUserResolver currentUserResolver) {
    this.careerAssistantService = careerAssistantService;
    this.currentUserResolver = currentUserResolver;
  }

  @PostMapping
  public ApiEnvelope<CareerAssistantResponse> ask(
      @Valid @RequestBody CareerAssistantRequest request) {
    return ApiEnvelope.success(
        careerAssistantService.ask(currentUserResolver.getCurrentUserId(), request.question()));
  }
}
