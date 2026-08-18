package com.devtrack.notifications.repository;

import com.devtrack.notifications.entity.Notification;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

  // Unread-first ordering — matches the actual query shape the notification
  // center needs, per 05_Database_Architecture.md §7's index note for this table.
  Page<Notification> findByUserIdOrderByReadAscCreatedAtDesc(UUID userId, Pageable pageable);

  @Modifying
  @Query("update Notification n set n.read = true where n.userId = :userId and n.read = false")
  void markAllReadForUser(@Param("userId") UUID userId);
}
