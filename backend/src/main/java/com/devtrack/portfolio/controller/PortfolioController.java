package com.devtrack.portfolio.controller;

import com.devtrack.common.dto.ApiEnvelope;
import com.devtrack.common.security.CurrentUserResolver;
import com.devtrack.portfolio.dto.request.UpdatePortfolioRequest;
import com.devtrack.portfolio.dto.response.PortfolioPublicView;
import com.devtrack.portfolio.dto.response.PortfolioSettingsResponse;
import com.devtrack.portfolio.service.PortfolioService;
import jakarta.validation.Valid;
import java.time.Duration;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/portfolio")
public class PortfolioController {

  private final PortfolioService portfolioService;
  private final CurrentUserResolver currentUserResolver;

  public PortfolioController(
      PortfolioService portfolioService,
      CurrentUserResolver currentUserResolver) {
    this.portfolioService = portfolioService;
    this.currentUserResolver = currentUserResolver;
  }

  @GetMapping("/me")
  public ApiEnvelope<PortfolioSettingsResponse> getMySettings() {
    return ApiEnvelope.success(
        portfolioService.getMySettings(
            currentUserResolver.getCurrentUserId()));
  }

  @PatchMapping("/me")
  public ApiEnvelope<PortfolioSettingsResponse> updateMySettings(
      @Valid @RequestBody UpdatePortfolioRequest request) {
    return ApiEnvelope.success(
        portfolioService.updateMySettings(
            currentUserResolver.getCurrentUserId(), request));
  }

  @GetMapping("/{slug}")
  public ResponseEntity<ApiEnvelope<PortfolioPublicView>> getPublic(
      @PathVariable String slug) {
    return ResponseEntity.ok()
        .cacheControl(
            CacheControl.maxAge(Duration.ofMinutes(5))
                .cachePublic()
                .staleWhileRevalidate(Duration.ofMinutes(1)))
        .body(
            ApiEnvelope.success(
                portfolioService.getPublicView(slug)));
  }
}