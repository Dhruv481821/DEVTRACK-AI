package com.devtrack.portfolio.dto.response;

import java.time.LocalDate;
import java.util.List;

public record PortfolioPublicView(
    String displayName,
    String headline,
    String bio,
    String avatarUrl,
    List<ProjectItem> projects,
    List<CertificateItem> certificates,
    GithubStats github) {

  public record ProjectItem(String name, String description) {}

  public record CertificateItem(
      String name, String issuingOrg, LocalDate issueDate, String verificationUrl) {}

  public record GithubStats(int repoCount, int totalStars, List<String> topLanguages) {}
}
