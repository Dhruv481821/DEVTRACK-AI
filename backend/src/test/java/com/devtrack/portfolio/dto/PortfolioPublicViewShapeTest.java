package com.devtrack.portfolio.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.devtrack.portfolio.dto.response.PortfolioPublicView;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class PortfolioPublicViewShapeTest {

  @Test
  void publicView_exposesExactlyTheAllowlistedFields() {
    assertThat(names(PortfolioPublicView.class))
        .containsExactly(
            "displayName",
            "headline",
            "bio",
            "avatarUrl",
            "projects",
            "certificates",
            "github");
  }

  @Test
  void projectItem_exposesNoIdsRepoLinksOrTimestamps() {
    assertThat(names(PortfolioPublicView.ProjectItem.class))
        .containsExactly("name", "description");
  }

  @Test
  void certificateItem_exposesNoIds() {
    assertThat(names(PortfolioPublicView.CertificateItem.class))
        .containsExactly(
            "name",
            "issuingOrg",
            "issueDate",
            "verificationUrl");
  }

  @Test
  void githubStats_exposesAggregatesOnly() {
    assertThat(names(PortfolioPublicView.GithubStats.class))
        .containsExactly("repoCount", "totalStars", "topLanguages");
  }

  private static List<String> names(Class<? extends Record> type) {
    return Arrays.stream(type.getRecordComponents())
        .map(RecordComponent::getName)
        .toList();
  }
}