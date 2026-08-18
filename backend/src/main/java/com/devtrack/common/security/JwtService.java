package com.devtrack.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

/**
 * RS256 access-token issuance/validation — the decision and reasoning (asymmetric
 * signing so a future verifying service never needs the signing key) is in
 * /docs/12_Security.md §2.2. Refresh tokens are NOT JWTs — they're opaque random
 * values, hashed at rest; see AuthService (next slice) and
 * /docs/05_Database_Architecture.md §7.
 */
@Service
@EnableConfigurationProperties(JwtProperties.class)
public class JwtService {

    // FR-AUTH-03 / 12_Security.md §3 — 15 minutes, not a magic number scattered
    // across the codebase.
    public static final Duration ACCESS_TOKEN_TTL = Duration.ofMinutes(15);

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public JwtService(JwtProperties properties) {
        this.privateKey = parsePrivateKey(properties.privateKey());
        this.publicKey = parsePublicKey(properties.publicKey());
    }

    /** No PII in the payload — see 12_Security.md §2.2: a JWT is base64, not encrypted. */
    public String issueAccessToken(UUID userId, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ACCESS_TOKEN_TTL)))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    /** Throws JwtException (expired, malformed, bad signature) — caller maps it, doesn't swallow it. */
    public Claims parseAndValidate(String token) throws JwtException {
        return Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token).getPayload();
    }

    private PrivateKey parsePrivateKey(String base64Pkcs8) {
        try {
            byte[] decoded = Base64.getDecoder().decode(base64Pkcs8);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(new PKCS8EncodedKeySpec(decoded));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            // Fail loudly at startup, not with a confusing NPE on the first login attempt.
            throw new IllegalStateException("Invalid JWT private key configuration", e);
        }
    }

    private PublicKey parsePublicKey(String base64X509) {
        try {
            byte[] decoded = Base64.getDecoder().decode(base64X509);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(new X509EncodedKeySpec(decoded));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Invalid JWT public key configuration", e);
        }
    }
}
