package com.devtrack.notes.repository;

import com.devtrack.notes.entity.Tag;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, UUID> {

  Optional<Tag> findByUserIdAndName(UUID userId, String name);

  List<Tag> findByUserId(UUID userId);
}
