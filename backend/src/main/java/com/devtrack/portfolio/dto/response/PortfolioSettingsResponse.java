package com.devtrack.portfolio.dto.response;

public record PortfolioSettingsResponse(
    String slug,
    String headline,
    boolean published
) {}