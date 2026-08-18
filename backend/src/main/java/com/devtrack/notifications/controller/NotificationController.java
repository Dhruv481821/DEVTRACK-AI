package com.devtrack.notifications.controller;

import com.devtrack.common.dto.ApiEnvelope;
import com.devtrack.common.security.CurrentUserResolver;
import com.devtrack.notifications.dto.response.NotificationResponse;
import com.devtrack.notifications.service.NotificationService;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * FR-NOTIF-01, per /docs/06_API_Specification.md §2.3. No producer endpoints here
 * by design — notifications are only ever created internally by event listeners
 * (04_System_Architecture.md §5), never via a client-facing POST.
 */
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUserResolver currentUserResolver;

    public NotificationController(
            NotificationService notificationService, CurrentUserResolver currentUserResolver) {
        this.notificationService = notificationService;
        this.currentUserResolver = currentUserResolver;
    }

    @GetMapping
    public ApiEnvelope<List<NotificationResponse>> list(Pageable pageable) {
        return ApiEnvelope.paginated(
                notificationService.listMyNotifications(currentUserResolver.getCurrentUserId(), pageable));
    }

    @PatchMapping("/{id}/read")
    public ApiEnvelope<Void> markRead(@PathVariable UUID id) {
        notificationService.markRead(id, currentUserResolver.getCurrentUserId());
        return ApiEnvelope.success(null);
    }

    @PatchMapping("/read-all")
    public ApiEnvelope<Void> markAllRead() {
        notificationService.markAllRead(currentUserResolver.getCurrentUserId());
        return ApiEnvelope.success(null);
    }
}
