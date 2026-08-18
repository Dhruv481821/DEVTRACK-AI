package com.devtrack.auth.service;

import com.devtrack.auth.dto.request.LoginRequest;
import com.devtrack.auth.dto.request.PasswordResetConfirmRequest;
import com.devtrack.auth.dto.request.RegisterRequest;
import com.devtrack.auth.dto.response.AuthResponse;
import com.devtrack.auth.entity.AppUser;
import com.devtrack.auth.entity.AuthProvider;
import com.devtrack.auth.entity.RefreshToken;
import com.devtrack.auth.entity.Role;
import com.devtrack.auth.entity.VerificationToken;
import com.devtrack.auth.entity.VerificationTokenType;
import com.devtrack.auth.repository.AppUserRepository;
import com.devtrack.auth.repository.RefreshTokenRepository;
import com.devtrack.auth.repository.RoleRepository;
import com.devtrack.auth.repository.VerificationTokenRepository;
import com.devtrack.common.email.EmailProperties;
import com.devtrack.common.email.EmailService;
import com.devtrack.common.events.UserRegisteredEvent;
import com.devtrack.common.exception.AuthenticationException;
import com.devtrack.common.exception.ConflictException;
import com.devtrack.common.exception.ResourceNotFoundException;
import com.devtrack.common.exception.ValidationException;
import com.devtrack.common.security.JwtService;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements FR-AUTH-01..05. Every branch here maps directly to a flow diagram in
 * /docs/12_Security.md §2 — read that first if a decision here looks arbitrary.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    // FR-AUTH-03 / 12_Security.md §3
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(30);
    // FR-AUTH-05 — 30 minute validity, matches 03_Software_Requirements_Specification.md
    private static final Duration RESET_TOKEN_TTL = Duration.ofMinutes(30);
    private static final Duration VERIFICATION_TOKEN_TTL = Duration.ofHours(24);

    private final AppUserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RoleRepository roleRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final EmailProperties emailProperties;
    private final ApplicationEventPublisher eventPublisher;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            AppUserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            RoleRepository roleRepository,
            VerificationTokenRepository verificationTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            EmailService emailService,
            EmailProperties emailProperties,
            ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.roleRepository = roleRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.emailProperties = emailProperties;
        this.eventPublisher = eventPublisher;
    }

    /** FR-AUTH-01. */
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.email())) {
            throw new ConflictException("An account with this email already exists.");
        }

        AppUser user = new AppUser();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setEmailVerified(false);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        assignDefaultRole(user);
        sendVerificationEmail(user);
        eventPublisher.publishEvent(new UserRegisteredEvent(user.getId()));
    }

    /** FR-AUTH-02 — password path. Google OAuth path lands in a follow-up slice. */
    @Transactional
    public AuthResponse.WithRefreshToken login(LoginRequest request) {
        AppUser user =
                userRepository
                        .findByEmailAndDeletedAtIsNull(request.email())
                        // Same generic message for "unknown email" and "wrong password" —
                        // deliberately, to avoid leaking which one it was (13_Testing.md §4's
                        // critical-path list calls this out explicitly).
                        .orElseThrow(() -> new AuthenticationException("AUTH_INVALID_CREDENTIALS", "Invalid email or password."));

        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthenticationException("AUTH_INVALID_CREDENTIALS", "Invalid email or password.");
        }

        return issueTokenPair(user);
    }

    /**
     * FR-AUTH-03 — rotation + reuse detection, per 12_Security.md §2.4. The reuse
     * branch matters more than the happy path: a revoked token being presented again
     * means either a client retry bug or a stolen token used after the legitimate
     * client already rotated past it. Since those can't be distinguished
     * server-side, the safe default is revoking the entire session family.
     */
    @Transactional
    public AuthResponse.WithRefreshToken refresh(String rawRefreshToken) {
        String tokenHash = hashToken(rawRefreshToken);
        RefreshToken existing =
                refreshTokenRepository
                        .findByTokenHash(tokenHash)
                        .orElseThrow(
                                () -> new AuthenticationException("AUTH_TOKEN_EXPIRED", "Refresh token is invalid or expired."));

        if (existing.getRevokedAt() != null || existing.getExpiresAt().isBefore(Instant.now())) {
            log.warn("Refresh token reuse or expiry detected for user {}", existing.getUser().getId());
            refreshTokenRepository.revokeAllActiveForUser(existing.getUser().getId());
            throw new AuthenticationException("AUTH_TOKEN_EXPIRED", "Refresh token is invalid or expired.");
        }

        existing.setRevokedAt(Instant.now());
        refreshTokenRepository.save(existing);

        return issueTokenPair(existing.getUser());
    }

    /** FR-AUTH-04. */
    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenRepository
                .findByTokenHash(hashToken(rawRefreshToken))
                .ifPresent(
                        token -> {
                            token.setRevokedAt(Instant.now());
                            refreshTokenRepository.save(token);
                        });
        // Intentionally not an error if the token is already gone/invalid — logout
        // is idempotent from the caller's perspective.
    }

    /** FR-AUTH-05, step 1. Always succeeds from the caller's perspective — never reveals whether the email exists. */
    @Transactional
    public void requestPasswordReset(String email) {
        userRepository
                .findByEmailAndDeletedAtIsNull(email)
                .ifPresent(
                        user -> {
                            String rawToken = generateOpaqueToken();
                            VerificationToken token = new VerificationToken();
                            token.setUser(user);
                            token.setTokenHash(hashToken(rawToken));
                            token.setType(VerificationTokenType.PASSWORD_RESET);
                            token.setExpiresAt(Instant.now().plus(RESET_TOKEN_TTL));
                            token.setCreatedAt(Instant.now());
                            verificationTokenRepository.save(token);

                            String link = emailProperties.frontend().baseUrl() + "/reset-password?token=" + rawToken;
                            emailService.sendPasswordResetEmail(user.getEmail(), link);
                        });
    }

    /**
     * FR-AUTH-05, step 2. Per 12_Security.md §2.5: a successful reset revokes every
     * existing refresh token for the user — a password reset is a reasonable signal
     * that any existing session should not be implicitly trusted to continue.
     */
    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        VerificationToken token = consumeToken(request.token(), VerificationTokenType.PASSWORD_RESET);

        AppUser user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        refreshTokenRepository.revokeAllActiveForUser(user.getId());
    }

    public void verifyEmail(String rawToken) {
        VerificationToken token = consumeToken(rawToken, VerificationTokenType.EMAIL_VERIFICATION);
        AppUser user = token.getUser();
        user.setEmailVerified(true);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
    }

    /**
     * FR-AUTH-02 — Google OAuth path. Per that requirement's original acceptance
     * criterion ("downstream code never branches on auth method"), this converges
     * on the same {@link #issueTokenPair} used by password login.
     *
     * <p><strong>Real security tradeoff, flagged rather than silently decided:</strong>
     * if a LOCAL (password) account already exists with this email, this method
     * logs into that existing account via Google rather than rejecting or requiring
     * explicit account-linking confirmation. This is a common pattern (trusting
     * Google's own email verification as equivalent to DevTrack's own), but it does
     * mean anyone who controls a given Google account can access a DevTrack account
     * registered with that same email via password — even without knowing the
     * password. Acceptable for v1 given Google verifies emails before allowing
     * sign-in, but worth knowing this exists rather than discovering it later. A
     * stricter alternative (reject with a "sign in with password instead" message,
     * or require an explicit linking confirmation step) is a reasonable v2
     * hardening if this product ever handles higher-stakes data.
     */
    @Transactional
    public AuthResponse.WithRefreshToken loginOrRegisterWithGoogle(String email) {
        AppUser user =
                userRepository
                        .findByEmailAndDeletedAtIsNull(email)
                        .orElseGet(
                                () -> {
                                    AppUser newUser = new AppUser();
                                    newUser.setEmail(email);
                                    newUser.setPasswordHash(null); // no password — Google-only account
                                    newUser.setAuthProvider(AuthProvider.GOOGLE);
                                    // Google has already verified this email as a precondition of the
                                    // OAuth flow succeeding — no need to run our own verification email.
                                    newUser.setEmailVerified(true);
                                    newUser.setCreatedAt(Instant.now());
                                    newUser.setUpdatedAt(Instant.now());
                                    userRepository.save(newUser);
                                    assignDefaultRole(newUser);
                                    eventPublisher.publishEvent(new UserRegisteredEvent(newUser.getId()));
                                    return newUser;
                                });

        return issueTokenPair(user);
    }

    // --- internal helpers -----------------------------------------------------

    private AuthResponse.WithRefreshToken issueTokenPair(AppUser user) {
        List<String> roles = user.getRoles().stream().map(Role::getName).toList();
        String accessToken = jwtService.issueAccessToken(user.getId(), roles);

        String rawRefreshToken = generateOpaqueToken();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(hashToken(rawRefreshToken));
        refreshToken.setExpiresAt(Instant.now().plus(REFRESH_TOKEN_TTL));
        refreshToken.setCreatedAt(Instant.now());
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse.WithRefreshToken(new AuthResponse(accessToken), rawRefreshToken);
    }

    private void assignDefaultRole(AppUser user) {
        Role userRole =
                roleRepository
                        .findByName("USER")
                        .orElseThrow(() -> new IllegalStateException("USER role missing — check V1 migration seed data"));
        user.getRoles().add(userRole);
        userRepository.save(user);
    }

    private void sendVerificationEmail(AppUser user) {
        String rawToken = generateOpaqueToken();
        VerificationToken token = new VerificationToken();
        token.setUser(user);
        token.setTokenHash(hashToken(rawToken));
        token.setType(VerificationTokenType.EMAIL_VERIFICATION);
        token.setExpiresAt(Instant.now().plus(VERIFICATION_TOKEN_TTL));
        token.setCreatedAt(Instant.now());
        verificationTokenRepository.save(token);

        String link = emailProperties.frontend().baseUrl() + "/verify-email?token=" + rawToken;
        emailService.sendVerificationEmail(user.getEmail(), link);
    }

    private VerificationToken consumeToken(String rawToken, VerificationTokenType expectedType) {
        VerificationToken token =
                verificationTokenRepository
                        .findByTokenHash(hashToken(rawToken))
                        .orElseThrow(() -> new ResourceNotFoundException("Token not found or already used."));

        if (token.getType() != expectedType) {
            throw new ValidationException("Token type mismatch.");
        }
        if (token.getUsedAt() != null) {
            throw new ValidationException("Token has already been used.");
        }
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new ValidationException("Token has expired.");
        }

        token.setUsedAt(Instant.now());
        verificationTokenRepository.save(token);
        return token;
    }

    /** Opaque, high-entropy, URL-safe — used for both refresh tokens and verification tokens. Never a JWT (those are only for access tokens). */
    private String generateOpaqueToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** SHA-256 of the raw token — the value actually persisted, per 12_Security.md §3: never store the raw token. */
    private String hashToken(String rawToken) {
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e); // never happens on any real JVM
        }
    }
}
