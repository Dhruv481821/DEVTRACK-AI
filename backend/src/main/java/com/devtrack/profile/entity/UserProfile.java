package com.devtrack.profile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Matches V1__phase0_foundation_schema.sql's user_profile table — user_id is both the primary key
 * and the foreign key (one-to-one with AppUser via a shared key, not a separate generated ID), per
 * the original schema design.
 */
@Entity
@Table(name = "user_profile")
@Getter
@Setter
@NoArgsConstructor
public class UserProfile {

  @Id
  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "display_name")
  private String displayName;

  @Column(name = "avatar_url")
  private String avatarUrl;

  @Column private String bio;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof UserProfile other)) return false;
    return userId != null && userId.equals(other.userId);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
