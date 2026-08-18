package com.devtrack.notifications.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

/**
 * Matches V1__phase0_foundation_schema.sql's notification table. userId is a plain
 * scalar column, deliberately NOT a @ManyToOne to AppUser — notifications and auth
 * are peer modules, and per 07_Backend_Architecture.md §4, modules don't reach into
 * each other's entities directly. Every query here only ever needs the ID, never
 * AppUser's other fields, so there's no real reason to pay for the cross-module
 * coupling a JPA relationship would create.
 *
 * <p>payload is a flat text field for v1, not a rigid schema per notification type
 * — per the migration's own note, there are zero real producers yet
 * (04_System_Architecture.md §3.5's event-bus consumers are inert until a Phase 2+
 * module publishes something), so this stays loose until a real shape is needed.
 */
@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String type;

    @Column
    private String payload;

    @Column(nullable = false)
    private boolean read = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
