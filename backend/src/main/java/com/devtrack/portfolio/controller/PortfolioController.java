package com.devtrack.portfolio.controller;

import com.devtrack.common.dto.ApiEnvelope;
import com.devtrack.common.exception.RateLimitExceededException;
import com.devtrack.common.security.CurrentUserResolver;
import com.devtrack.common.security.RateLimiterService;
import com.devtrack.portfolio.dto.request.UpdatePortfolioRequest;
import com.devtrack.portfolio.dto.response.PortfolioPublicView;
import com.devtrack.portfolio.dto.response.PortfolioSettingsResponse;
import com.devtrack.portfolio.service.PortfolioService;
import jakarta.servlet.http.HttpServletRequest;
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

  // The one unauthenticated route (12_Security.md threat table: enumeration/scraping). Generous
  // enough for real visitors; the edge cache (Cache-Control below) absorbs normal read traffic.
  static final int PUBLIC_REQUESTS_PER_MINUTE_PER_IP = 60;

  private final PortfolioService portfolioService;
  private final CurrentUserResolver currentUserResolver;
  private final RateLimiterService rateLimiterService;

  public PortfolioController(
      PortfolioService portfolioService,
      CurrentUserResolver currentUserResolver,
      RateLimiterService rateLimiterService) {
    this.portfolioService = portfolioService;
    this.currentUserResolver = currentUserResolver;
    this.rateLimiterService = rateLimiterService;
  }

  @GetMapping("/me")
  public ApiEnvelope<PortfolioSettingsResponse> getMySettings() {
    return ApiEnvelope.success(
        portfolioService.getMySettings(currentUserResolver.getCurrentUserId()));
  }

  @PatchMapping("/me")
  public ApiEnvelope<PortfolioSettingsResponse> updateMySettings(
      @Valid @RequestBody UpdatePortfolioRequest request) {
    return ApiEnvelope.success(
        portfolioService.updateMySettings(currentUserResolver.getCurrentUserId(), request));
  }

  @GetMapping("/{slug}")
  public ResponseEntity<ApiEnvelope<PortfolioPublicView>> getPublic(
      @PathVariable String slug, HttpServletRequest request) {
    // getRemoteAddr is the real client IP in prod: application-prod.yml sets
    // forward-headers-strategy: framework, same basis as AuthController's limiter.
    if (!rateLimiterService.tryConsumePerMinute(
        "portfolio-public:" + request.getRemoteAddr(), PUBLIC_REQUESTS_PER_MINUTE_PER_IP)) {
      throw new RateLimitExceededException("Too many requests. Please try again in a minute.");
    }
    return ResponseEntity.ok()
        .cacheControl(
            CacheControl.maxAge(Duration.ofMinutes(5))
                .cachePublic()
                .staleWhileRevalidate(Duration.ofMinutes(1)))
        .body(ApiEnvelope.success(portfolioService.getPublicView(slug)));
  }
}
