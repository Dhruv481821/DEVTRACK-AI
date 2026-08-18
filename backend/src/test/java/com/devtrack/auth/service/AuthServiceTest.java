package com.devtrack.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devtrack.auth.dto.request.LoginRequest;
import com.devtrack.auth.dto.request.RegisterRequest;
import com.devtrack.auth.dto.response.AuthResponse;
import com.devtrack.auth.entity.AppUser;
import com.devtrack.auth.entity.RefreshToken;
import com.devtrack.auth.entity.Role;
import com.devtrack.auth.repository.AppUserRepository;
import com.devtrack.auth.repository.RefreshTokenRepository;
import com.devtrack.auth.repository.RoleRepository;
import com.devtrack.auth.repository.VerificationTokenRepository;
import com.devtrack.common.email.EmailProperties;
import com.devtrack.common.email.EmailService;
import com.devtrack.common.events.UserRegisteredEvent;
import com.devtrack.common.exception.AuthenticationException;
import com.devtrack.common.exception.ConflictException;
import com.devtrack.common.security.JwtService;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Unit tests (repositories/services mocked, per /docs/13_Testing.md §2's "unit (service layer)"
 * row) — no real DB needed, so these run fast and don't depend on Testcontainers being available.
 * Repository-layer integration tests against a real Postgres are a separate, later addition.
 *
 * <p>Every test here maps to an explicit item in /docs/13_Testing.md §4's critical-path list — that
 * list is the actual spec for what this file must cover.
 */
class AuthServiceTest {

  private AppUserRepository userRepository;
  private RefreshTokenRepository refreshTokenRepository;
  private RoleRepository roleRepository;
  private VerificationTokenRepository verificationTokenRepository;
  private PasswordEncoder passwordEncoder;
  private JwtService jwtService;
  private EmailService emailService;
  private ApplicationEventPublisher eventPublisher;
  private AuthService authService;

  @BeforeEach
  void setUp() {
    userRepository = mock(AppUserRepository.class);
    refreshTokenRepository = mock(RefreshTokenRepository.class);
    roleRepository = mock(RoleRepository.class);
    verificationTokenRepository = mock(VerificationTokenRepository.class);
    passwordEncoder = mock(PasswordEncoder.class);
    jwtService = mock(JwtService.class);
    emailService = mock(EmailService.class);
    eventPublisher = mock(ApplicationEventPublisher.class);
    EmailProperties emailProperties =
        new EmailProperties(
            new EmailProperties.Resend("test-key"),
            new EmailProperties.Frontend("http://localhost:5173"));

    authService =
        new AuthService(
            userRepository,
            refreshTokenRepository,
            roleRepository,
            verificationTokenRepository,
            passwordEncoder,
            jwtService,
            emailService,
            emailProperties,
            eventPublisher);
  }

  // --- FR-AUTH-01: registration ------------------------------------------------

  @Test
  void register_withDuplicateEmail_throwsConflict() {
    when(userRepository.existsByEmailAndDeletedAtIsNull("taken@example.com")).thenReturn(true);

    assertThatThrownBy(
            () -> authService.register(new RegisterRequest("taken@example.com", "Password1")))
        .isInstanceOf(ConflictException.class);

    verify(userRepository, never()).save(any());
  }

  @Test
  void register_withNewEmail_savesUserAssignsRoleAndSendsVerificationEmail() {
    when(userRepository.existsByEmailAndDeletedAtIsNull("new@example.com")).thenReturn(false);
    when(passwordEncoder.encode("Password1")).thenReturn("hashed");
    Role userRole = new Role();
    userRole.setId(UUID.randomUUID());
    userRole.setName("USER");
    when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));

    // A mocked repository doesn't simulate Hibernate's @UuidGenerator assigning
    // an ID on first persist the way a real save() would — without this stub,
    // the event-publishing assertion below would be tautological (comparing
    // null to null) rather than a genuine test of "the published event carries
    // the newly created user's real ID."
    UUID generatedId = UUID.randomUUID();
    when(userRepository.save(any(AppUser.class)))
        .thenAnswer(
            invocation -> {
              AppUser u = invocation.getArgument(0);
              if (u.getId() == null) {
                u.setId(generatedId);
              }
              return u;
            });

    authService.register(new RegisterRequest("new@example.com", "Password1"));

    ArgumentCaptor<AppUser> savedUser = ArgumentCaptor.forClass(AppUser.class);
    // save() is called twice: once on initial persist, once after role assignment
    verify(userRepository, times(2)).save(savedUser.capture());
    assertThat(savedUser.getValue().getRoles()).contains(userRole);
    verify(emailService).sendVerificationEmail(any(), any());

    // Real assertion, not just a compile fix — the whole point of the event
    // bus (04_System_Architecture.md §3.5) is that UserProfileInitializer
    // reacts to this without AuthService knowing it exists; this is the
    // contract that guarantees that listener ever fires with the right ID.
    ArgumentCaptor<UserRegisteredEvent> publishedEvent =
        ArgumentCaptor.forClass(UserRegisteredEvent.class);
    verify(eventPublisher).publishEvent(publishedEvent.capture());
    assertThat(publishedEvent.getValue().userId()).isEqualTo(generatedId);
  }

  // --- FR-AUTH-02: login, and the enumeration-safety requirement --------------

  @Test
  void login_withUnknownEmail_throwsGenericAuthError() {
    when(userRepository.findByEmailAndDeletedAtIsNull("ghost@example.com"))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.login(new LoginRequest("ghost@example.com", "whatever")))
        .isInstanceOf(AuthenticationException.class)
        .hasMessage("Invalid email or password.");
  }

  @Test
  void login_withWrongPassword_throwsSameGenericAuthError() {
    // Critical: this test and the one above must throw the SAME message —
    // 13_Testing.md §4 calls this out explicitly. Differing messages leak
    // whether an email is registered.
    AppUser user = existingUser("real@example.com", "hashed");
    when(userRepository.findByEmailAndDeletedAtIsNull("real@example.com"))
        .thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

    assertThatThrownBy(() -> authService.login(new LoginRequest("real@example.com", "wrong")))
        .isInstanceOf(AuthenticationException.class)
        .hasMessage("Invalid email or password.");
  }

  @Test
  void login_withCorrectCredentials_issuesAccessAndRefreshTokens() {
    AppUser user = existingUser("real@example.com", "hashed");
    when(userRepository.findByEmailAndDeletedAtIsNull("real@example.com"))
        .thenReturn(Optional.of(user));
    when(passwordEncoder.matches("correct", "hashed")).thenReturn(true);
    when(jwtService.issueAccessToken(any(), any())).thenReturn("fake.jwt.token");

    AuthResponse.WithRefreshToken result =
        authService.login(new LoginRequest("real@example.com", "correct"));

    assertThat(result.body().accessToken()).isEqualTo("fake.jwt.token");
    assertThat(result.rawRefreshToken()).isNotBlank();
    verify(refreshTokenRepository).save(any(RefreshToken.class));
  }

  // --- FR-AUTH-03: the most important test in the suite (13_Testing.md §4) ----

  @Test
  void refresh_withValidUnrevokedToken_rotatesAndIssuesNewPair() {
    AppUser user = existingUser("real@example.com", "hashed");
    RefreshToken existing = new RefreshToken();
    existing.setId(UUID.randomUUID());
    existing.setUser(user);
    existing.setExpiresAt(Instant.now().plusSeconds(3600));
    existing.setRevokedAt(null);
    when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(existing));
    when(jwtService.issueAccessToken(any(), any())).thenReturn("new.jwt.token");

    AuthResponse.WithRefreshToken result = authService.refresh("raw-token-value");

    assertThat(existing.getRevokedAt()).isNotNull(); // old token marked revoked
    assertThat(result.body().accessToken()).isEqualTo("new.jwt.token");
    assertThat(result.rawRefreshToken()).isNotBlank();
    verify(refreshTokenRepository, never())
        .revokeAllActiveForUser(any()); // NOT the reuse-detection path
  }

  @Test
  void refresh_withAlreadyRevokedToken_revokesEntireSessionFamilyAndThrows() {
    // This is the reuse-detection branch — 12_Security.md §2.4's core guarantee.
    AppUser user = existingUser("real@example.com", "hashed");
    RefreshToken alreadyRevoked = new RefreshToken();
    alreadyRevoked.setId(UUID.randomUUID());
    alreadyRevoked.setUser(user);
    alreadyRevoked.setExpiresAt(Instant.now().plusSeconds(3600));
    alreadyRevoked.setRevokedAt(
        Instant.now().minusSeconds(60)); // already revoked — this IS the reuse
    when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(alreadyRevoked));

    assertThatThrownBy(() -> authService.refresh("stolen-or-replayed-token"))
        .isInstanceOf(AuthenticationException.class);

    verify(refreshTokenRepository).revokeAllActiveForUser(user.getId());
  }

  @Test
  void refresh_withExpiredToken_revokesEntireSessionFamilyAndThrows() {
    AppUser user = existingUser("real@example.com", "hashed");
    RefreshToken expired = new RefreshToken();
    expired.setId(UUID.randomUUID());
    expired.setUser(user);
    expired.setExpiresAt(Instant.now().minusSeconds(60)); // expired
    expired.setRevokedAt(null);
    when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(expired));

    assertThatThrownBy(() -> authService.refresh("expired-token"))
        .isInstanceOf(AuthenticationException.class);

    verify(refreshTokenRepository).revokeAllActiveForUser(user.getId());
  }

  @Test
  void refresh_withUnknownToken_throwsWithoutTouchingRevocation() {
    when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.refresh("never-issued-token"))
        .isInstanceOf(AuthenticationException.class);

    verify(refreshTokenRepository, never()).revokeAllActiveForUser(any());
  }

  // --- FR-AUTH-04: logout is idempotent ----------------------------------------

  @Test
  void logout_withUnknownToken_doesNotThrow() {
    when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

    authService.logout("already-gone-token"); // must not throw
  }

  // --- helpers -----------------------------------------------------------------

  private AppUser existingUser(String email, String passwordHash) {
    AppUser user = new AppUser();
    user.setId(UUID.randomUUID());
    user.setEmail(email);
    user.setPasswordHash(passwordHash);
    return user;
  }
}
