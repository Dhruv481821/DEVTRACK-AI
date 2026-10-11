package com.devtrack.portfolio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "portfolio")
@Getter
@Setter
@NoArgsConstructor
public class Portfolio {

  @Id
  @Column(name = "user_id")
  private UUID userId;

  @Column(nullable = false)
  private String slug;

  @Column
  private String headline;

  @Column(nullable = false)
  private boolean published = false;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Portfolio other)) return false;
    return userId != null && userId.equals(other.userId);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}