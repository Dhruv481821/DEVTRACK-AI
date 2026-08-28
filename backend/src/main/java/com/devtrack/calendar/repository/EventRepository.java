package com.devtrack.calendar.repository;

import com.devtrack.calendar.entity.Event;
import com.devtrack.calendar.entity.EventSource;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, UUID> {

  // FR-CAL-01 — a date range for a given user, matching the index in
  // 05_Database_Architecture.md §9.
  List<Event> findByUserIdAndStartAtBetweenOrderByStartAtAsc(UUID userId, Instant from, Instant to);

  // Backs the listener's upsert logic (05_Database_Architecture.md §10) — find
  // the existing auto-generated event for a given source row, if one exists,
  // so a repeat event updates it instead of creating a duplicate.
  Optional<Event> findBySourceAndSourceId(EventSource source, UUID sourceId);
}
