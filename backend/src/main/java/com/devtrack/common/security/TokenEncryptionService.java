package com.devtrack.common.security;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * AES-256-GCM application-layer encryption for third-party tokens at rest — the decision from
 * /docs/12_Security.md §4, flagged there as "a concrete Phase 2 implementation task," now due for
 * github_connection.access_token_encrypted.
 *
 * <p>Defense-in-depth reasoning restated here since it's the whole point of this class existing:
 * Neon encrypts data at rest by default, but that protects against physical disk theft, not a SQL
 * injection or a misconfigured read-replica exposing a table read. Encrypting at this layer means
 * even a full row read doesn't yield a usable GitHub token without this application's own key,
 * which lives in environment configuration, not the database.
 *
 * <p>GCM mode is authenticated encryption — it detects tampering, not just confidentiality. A
 * random 12-byte IV is generated per encryption call and stored alongside the ciphertext
 * (prepended), since GCM requires a unique IV per encryption under the same key but the IV itself
 * isn't secret.
 */
@Service
public class TokenEncryptionService {

  private static final String ALGORITHM = "AES/GCM/NoPadding";
  private static final int GCM_IV_LENGTH_BYTES = 12;
  private static final int GCM_TAG_LENGTH_BITS = 128;

  private final SecretKeySpec key;
  private final SecureRandom secureRandom = new SecureRandom();

  public TokenEncryptionService(@Value("${devtrack.token-encryption-key}") String base64Key) {
    byte[] decoded = Base64.getDecoder().decode(base64Key);
    this.key = new SecretKeySpec(decoded, "AES");
  }

  public String encrypt(String plaintext) {
    try {
      byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
      secureRandom.nextBytes(iv);

      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));

      byte[] ciphertext =
          cipher.doFinal(plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8));

      ByteBuffer buffer = ByteBuffer.allocate(iv.length + ciphertext.length);
      buffer.put(iv).put(ciphertext);

      return Base64.getEncoder().encodeToString(buffer.array());
    } catch (Exception e) {
      // Fail loudly — a token that can't be encrypted must never be stored in plaintext instead.
      throw new IllegalStateException("Failed to encrypt token", e);
    }
  }

  public String decrypt(String encoded) {
    try {
      byte[] combined = Base64.getDecoder().decode(encoded);
      ByteBuffer buffer = ByteBuffer.wrap(combined);

      byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
      buffer.get(iv);

      byte[] ciphertext = new byte[buffer.remaining()];
      buffer.get(ciphertext);

      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));

      return new String(cipher.doFinal(ciphertext), java.nio.charset.StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to decrypt token", e);
    }
  }
}
