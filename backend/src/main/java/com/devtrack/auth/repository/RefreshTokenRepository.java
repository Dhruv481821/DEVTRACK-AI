package com.devtrack.auth.repository;

import com.devtrack.auth.entity.RefreshToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    // Supports 12_Security.md §2.4's defensive "revoke everything" branch when
    // token reuse is detected — one bulk update, not N individual saves.
    @Modifying
    @Query(
            "update RefreshToken r set r.revokedAt = current_timestamp "
                    + "where r.user.id = :userId and r.revokedAt is null")
    void revokeAllActiveForUser(@Param("userId") UUID userId);
}
