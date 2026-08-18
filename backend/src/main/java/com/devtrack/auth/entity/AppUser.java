package com.devtrack.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

/**
 * Matches V1__phase0_foundation_schema.sql's app_user table exactly.
 *
 * <p>No {@code @Data} here — see /docs/07_Backend_Architecture.md §3 and
 * /docs/17_Coding_Standards.md §2: explicit accessors, ID-only equality, no
 * relationship fields in toString (this entity has none yet, but the convention
 * holds for every entity added later).
 */
@Entity
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    /** Nullable — Google OAuth users have no password (auth_provider = GOOGLE). */
    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING) // never ORDINAL — reordering the enum later would silently corrupt stored data
    @Column(name = "auth_provider", nullable = false)
    private AuthProvider authProvider;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    // v1 always contains exactly one Role ('USER') — the collection mapping exists
    // so RBAC is a real, extensible mechanism from day one, per
    // /docs/04_System_Architecture.md's "extensible interface, not a role-management
    // UI with nothing to manage" decision. Not exposed via any admin endpoint yet.
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppUser other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
