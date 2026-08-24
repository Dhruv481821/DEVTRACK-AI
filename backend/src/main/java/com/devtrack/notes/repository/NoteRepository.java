package com.devtrack.notes.repository;

import com.devtrack.notes.entity.Note;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, UUID> {

  // FR-NOTES-01 — most-recently-updated first, matching the actual notes-list
  // query shape (05_Database_Architecture.md §9's index note).
  Page<Note> findByUserIdOrderByUpdatedAtDesc(UUID userId, Pageable pageable);

  // Search (FR-NOTES-02) is deliberately NOT implemented here yet — see
  // NoteService's docblock for why this is its own separate, later slice rather
  // than a naive LIKE query that would contradict 05_Database_Architecture.md
  // §4's already-decided full-text-search architecture.
}
