package com.devtrack.portfolio.service;

import com.devtrack.certificates.dto.response.CertificateResponse;
import com.devtrack.certificates.service.CertificateService;
import com.devtrack.common.exception.ConflictException;
import com.devtrack.common.exception.ResourceNotFoundException;
import com.devtrack.common.exception.ValidationException;
import com.devtrack.github.service.GithubSummaryService;
import com.devtrack.portfolio.dto.request.UpdatePortfolioRequest;
import com.devtrack.portfolio.dto.response.PortfolioPublicView;
import com.devtrack.portfolio.dto.response.PortfolioSettingsResponse;
import com.devtrack.portfolio.entity.Portfolio;
import com.devtrack.portfolio.repository.PortfolioRepository;
import com.devtrack.profile.dto.response.ProfileResponse;
import com.devtrack.profile.service.ProfileService;
import com.devtrack.project.service.ProjectService;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortfolioService {

  private static final Set<String> RESERVED_SLUGS =
      Set.of("me", "api", "admin", "login");

  private final PortfolioRepository portfolioRepository;
  private final ProfileService profileService;
  private final ProjectService projectService;
  private final CertificateService certificateService;
  private final GithubSummaryService githubSummaryService;

  public PortfolioService(
      PortfolioRepository portfolioRepository,
      ProfileService profileService,
      ProjectService projectService,
      CertificateService certificateService,
      GithubSummaryService githubSummaryService) {
    this.portfolioRepository = portfolioRepository;
    this.profileService = profileService;
    this.projectService = projectService;
    this.certificateService = certificateService;
    this.githubSummaryService = githubSummaryService;
  }

  @Transactional(readOnly = true)
  public PortfolioSettingsResponse getMySettings(UUID userId) {
    return portfolioRepository
        .findById(userId)
        .map(this::toSettings)
        .orElseGet(
            () -> new PortfolioSettingsResponse(null, null, false));
  }

  @Transactional
  public PortfolioSettingsResponse updateMySettings(
      UUID userId, UpdatePortfolioRequest request) {

    Portfolio portfolio =
        portfolioRepository.findById(userId)
            .orElseGet(Portfolio::new);

    boolean isNew = portfolio.getUserId() == null;

    if (isNew) {
      portfolio.setUserId(userId);
      portfolio.setCreatedAt(Instant.now());
    }

    if (request.slug() != null
        && !request.slug().equals(portfolio.getSlug())) {

      if (RESERVED_SLUGS.contains(request.slug())
          || portfolioRepository.existsBySlug(request.slug())) {
        throw new ConflictException(
            "That portfolio URL is already taken.");
      }

      portfolio.setSlug(request.slug());
    }

    if (request.headline() != null) {
      portfolio.setHeadline(request.headline());
    }

    if (request.published() != null) {
      portfolio.setPublished(request.published());
    }

    if (portfolio.getSlug() == null) {
      throw new ValidationException(
          "Choose a portfolio URL first.");
    }

    portfolio.setUpdatedAt(Instant.now());

    try {
      portfolioRepository.saveAndFlush(portfolio);
    } catch (DataIntegrityViolationException e) {
      throw new ConflictException(
          "That portfolio URL is already taken.");
    }

    return toSettings(portfolio);
  }

  @Transactional(readOnly = true)
  public PortfolioPublicView getPublicView(String slug) {

    Portfolio portfolio =
        portfolioRepository.findBySlug(slug)
            .filter(Portfolio::isPublished)
            .orElseThrow(
                () -> new ResourceNotFoundException(
                    "Portfolio not found."));

    UUID userId = portfolio.getUserId();

    ProfileResponse profile =
        profileService.getMyProfile(userId);

    List<PortfolioPublicView.ProjectItem> projects =
        projectService.getProjects(userId).stream()
            .map(p -> new PortfolioPublicView.ProjectItem(
                p.name(), p.description()))
            .toList();

    List<PortfolioPublicView.CertificateItem> certificates =
        certificateService.listMyCertificates(userId).stream()
            .map(this::toCertificateItem)
            .toList();

    PortfolioPublicView.GithubStats github =
        githubSummaryService.summarize(userId)
            .map(s -> new PortfolioPublicView.GithubStats(
                s.repoCount(), s.totalStars(), s.topLanguages()))
            .orElse(null);

    return new PortfolioPublicView(
        profile.displayName(),
        blankToNull(portfolio.getHeadline()),
        profile.bio(),
        safeHttpUrlOrNull(profile.avatarUrl()),
        projects,
        certificates,
        github);
  }

  private PortfolioPublicView.CertificateItem toCertificateItem(
      CertificateResponse c) {
    return new PortfolioPublicView.CertificateItem(
        c.name(),
        c.issuingOrg(),
        c.issueDate(),
        safeHttpUrlOrNull(c.verificationUrl()));
  }

  private PortfolioSettingsResponse toSettings(Portfolio portfolio) {
    return new PortfolioSettingsResponse(
        portfolio.getSlug(),
        portfolio.getHeadline(),
        portfolio.isPublished());
  }

  private static String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }

  static String safeHttpUrlOrNull(String url) {
    if (url == null) {
      return null;
    }

    String lower = url.trim().toLowerCase();

    return lower.startsWith("https://")
            || lower.startsWith("http://")
        ? url.trim()
        : null;
  }
}