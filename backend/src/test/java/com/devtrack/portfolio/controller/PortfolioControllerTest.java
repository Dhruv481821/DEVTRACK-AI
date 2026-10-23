package com.devtrack.portfolio.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.devtrack.common.exception.RateLimitExceededException;
import com.devtrack.common.security.CurrentUserResolver;
import com.devtrack.common.security.RateLimiterService;
import com.devtrack.portfolio.dto.response.PortfolioPublicView;
import com.devtrack.portfolio.service.PortfolioService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class PortfolioControllerTest {

  private PortfolioService portfolioService;
  private RateLimiterService rateLimiterService;
  private PortfolioController controller;
  private MockHttpServletRequest request;

  @BeforeEach
  void setUp() {
    portfolioService = mock(PortfolioService.class);
    rateLimiterService = mock(RateLimiterService.class);
    controller =
        new PortfolioController(
            portfolioService, mock(CurrentUserResolver.class), rateLimiterService);
    request = new MockHttpServletRequest();
    request.setRemoteAddr("203.0.113.7");
  }

  @Test
  void getPublic_overTheLimit_throwsWithoutReadingThePortfolio() {
    when(rateLimiterService.tryConsumePerMinute(eq("portfolio-public:203.0.113.7"), anyInt()))
        .thenReturn(false);

    assertThatThrownBy(() -> controller.getPublic("dhruv", request))
        .isInstanceOf(RateLimitExceededException.class);
    verifyNoInteractions(portfolioService);
  }

  @Test
  void getPublic_underTheLimit_returnsCacheablePublicView() {
    when(rateLimiterService.tryConsumePerMinute(
            "portfolio-public:203.0.113.7", PortfolioController.PUBLIC_REQUESTS_PER_MINUTE_PER_IP))
        .thenReturn(true);
    PortfolioPublicView view =
        new PortfolioPublicView("Dhruv", null, null, null, List.of(), List.of(), null);
    when(portfolioService.getPublicView("dhruv")).thenReturn(view);

    var response = controller.getPublic("dhruv", request);

    assertThat(response.getStatusCode().value()).isEqualTo(200);
    assertThat(response.getHeaders().getCacheControl()).contains("public").contains("max-age=300");
  }
}
