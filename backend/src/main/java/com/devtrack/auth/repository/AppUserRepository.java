package com.devtrack.auth.repository;

import com.devtrack.auth.entity.AppUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

  // Optional as a return type only, never a field/param — 17_Coding_Standards.md §2.
  Optional<AppUser> findByEmailAndDeletedAtIsNull(String email);

  boolean existsByEmailAndDeletedAtIsNull(String email);
}
