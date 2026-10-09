package com.devtrack.project.repository;

import com.devtrack.project.entity.Project;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

  List<Project> findByUserIdAndDeletedAtIsNullOrderByUpdatedAtDesc(UUID userId);

  Optional<Project> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);
}
