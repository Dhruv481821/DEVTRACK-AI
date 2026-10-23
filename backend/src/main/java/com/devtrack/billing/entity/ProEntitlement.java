package com.devtrack.billing.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Shared-key row (user_id is the PK, same shape as UserProfile). Pro while proUntil > now. */
@Entity
@Table(name = "pro_entitlement")
@Getter
@Setter
@NoArgsConstructor
public class ProEntitlement {

  @Id
  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "pro_until", nullable = false)
  private Instant proUntil;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ProEntitlement other)) return false;
    return userId != null && userId.equals(other.userId);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
