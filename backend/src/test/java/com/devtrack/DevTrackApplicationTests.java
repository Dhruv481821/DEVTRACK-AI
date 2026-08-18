package com.devtrack;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Boots the full application context — including all JPA entities/repositories now that the auth
 * module exists — so it genuinely needs a real Postgres to start at all, regardless of Spring
 * profile. Per /docs/13_Testing.md §2: "Integration (repository) — Testcontainers (real Postgres),
 * not H2." A real, ephemeral container is provisioned here rather than depending on an active "dev"
 * profile and a manually-running local Postgres, which is what caused this test to fail with
 * "Failed to determine a suitable driver class" — see the diagnosis in this turn's chat response
 * for the full root-cause chain.
 *
 * <p>Activates the "dev" profile specifically to load its JWT keypair, CORS origin, and email
 * config (application-dev.yml) — without this, JwtService's constructor NPEs trying to
 * Base64-decode a null private key, since those properties only exist in the dev/prod profile
 * files, never the base application.yml. The datasource is still fully controlled
 * by @DynamicPropertySource below, not by dev's own (irrelevant here) datasource block — dynamic
 * properties always take precedence over statically-configured ones, so there's no conflict.
 *
 * <p>Requires Docker (or a compatible container runtime) to be running locally — that's a genuine
 * prerequisite of the Testcontainers approach this project already committed to, not something this
 * fix can avoid.
 */
@Testcontainers
@SpringBootTest
@ActiveProfiles("dev")
class DevTrackApplicationTests {

  @Container
  static PostgreSQLContainer postgres =
      new PostgreSQLContainer(DockerImageName.parse("postgres:16"));

  @DynamicPropertySource
  static void datasourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Test
  void contextLoads() {
    assertThat(true).isTrue();
  }
}
