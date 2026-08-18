package com.devtrack.notifications.service;

import com.devtrack.common.exception.ResourceNotFoundException;
import com.devtrack.common.security.OwnershipGuard;
import com.devtrack.notifications.dto.response.NotificationResponse;
import com.devtrack.notifications.entity.Notification;
import com.devtrack.notifications.repository.NotificationRepository;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** FR-NOTIF-01. */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final OwnershipGuard ownershipGuard;

    public NotificationService(NotificationRepository notificationRepository, OwnershipGuard ownershipGuard) {
        this.notificationRepository = notificationRepository;
        this.ownershipGuard = ownershipGuard;
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> listMyNotifications(UUID userId, Pageable pageable) {
        return notificationRepository
                .findByUserIdOrderByReadAscCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    /**
     * The first real use of OwnershipGuard (06_API_Specification.md §1.6) — a
     * notification ID belonging to a different user returns 404, not 403, so a
     * caller can't distinguish "not yours" from "doesn't exist."
     */
    @Transactional
    public void markRead(UUID notificationId, UUID requestingUserId) {
        Notification notification =
                notificationRepository
                        .findById(notificationId)
                        .orElseThrow(() -> new ResourceNotFoundException("Notification not found."));
        ownershipGuard.assertOwnedBy(notification.getUserId(), requestingUserId);

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllRead(UUID userId) {
        notificationRepository.markAllReadForUser(userId);
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(n.getId(), n.getType(), n.getPayload(), n.isRead(), n.getCreatedAt());
    }
}
