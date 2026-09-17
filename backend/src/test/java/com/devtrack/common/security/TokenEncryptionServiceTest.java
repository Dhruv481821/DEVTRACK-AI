package com.devtrack.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.security.SecureRandom;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Not just "does it run" — these verify the actual security properties TokenEncryptionService's
 * docblock claims: random IVs (not a fixed/reused one) and GCM's tamper detection, both of which
 * would silently fail to be true if the implementation had a subtle bug, even if a naive round-trip
 * test still passed.
 */
class TokenEncryptionServiceTest {

  private TokenEncryptionService tokenEncryptionService;

  @BeforeEach
  void setUp() {
    byte[] key = new byte[32];
    new SecureRandom().nextBytes(key);
    tokenEncryptionService = new TokenEncryptionService(Base64.getEncoder().encodeToString(key));
  }

  @Test
  void encryptThenDecrypt_returnsOriginalPlaintext() {
    String original = "ghp_realisticLookingGithubTokenValue1234567890";

    String encrypted = tokenEncryptionService.encrypt(original);
    String decrypted = tokenEncryptionService.decrypt(encrypted);

    assertThat(decrypted).isEqualTo(original);
  }

  /**
   * Proves a real random IV is generated per call, not a fixed one — a fixed IV would be a genuine
   * GCM security flaw.
   */
  @Test
  void encryptingSamePlaintextTwice_producesDifferentCiphertext() {
    String plaintext = "same-token-value";

    String encryptedFirst = tokenEncryptionService.encrypt(plaintext);

    String encryptedSecond = tokenEncryptionService.encrypt(plaintext);

    assertThat(encryptedFirst).isNotEqualTo(encryptedSecond);

    // Both still decrypt correctly, proving the IV is stored/read
    // correctly alongside each ciphertext.
    assertThat(tokenEncryptionService.decrypt(encryptedFirst)).isEqualTo(plaintext);

    assertThat(tokenEncryptionService.decrypt(encryptedSecond)).isEqualTo(plaintext);
  }

  /** GCM's authentication tag — tampering must be detected, not silently decrypt to garbage. */
  @Test
  void decrypt_withTamperedCiphertext_throws() {
    String encrypted = tokenEncryptionService.encrypt("a real token");

    byte[] tampered = Base64.getDecoder().decode(encrypted);

    tampered[tampered.length - 1] ^= 0xFF;

    String tamperedEncoded = Base64.getEncoder().encodeToString(tampered);

    assertThatThrownBy(() -> tokenEncryptionService.decrypt(tamperedEncoded))
        .isInstanceOf(IllegalStateException.class);
  }
}
