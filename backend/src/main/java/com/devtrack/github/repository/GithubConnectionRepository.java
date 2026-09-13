package com.devtrack.github.repository;

import com.devtrack.github.entity.GithubConnection;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GithubConnectionRepository
        extends JpaRepository<GithubConnection, UUID> {

    Optional<GithubConnection> findByUserId(UUID userId);
}