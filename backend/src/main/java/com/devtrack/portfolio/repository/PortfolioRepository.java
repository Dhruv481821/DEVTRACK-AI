package com.devtrack.portfolio.repository;

import com.devtrack.portfolio.entity.Portfolio;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository
    extends JpaRepository<Portfolio, UUID> {

  Optional<Portfolio> findBySlug(String slug);

  boolean existsBySlug(String slug);
}