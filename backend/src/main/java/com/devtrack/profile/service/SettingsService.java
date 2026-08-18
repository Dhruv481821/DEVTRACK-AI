package com.devtrack.profile.service;

import com.devtrack.common.exception.ResourceNotFoundException;
import com.devtrack.profile.dto.request.UpdateSettingsRequest;
import com.devtrack.profile.dto.response.SettingsResponse;
import com.devtrack.profile.entity.UserSettings;
import com.devtrack.profile.repository.UserSettingsRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** FR-SET-01. */
@Service
public class SettingsService {

  private final UserSettingsRepository userSettingsRepository;

  public SettingsService(UserSettingsRepository userSettingsRepository) {
    this.userSettingsRepository = userSettingsRepository;
  }

  @Transactional(readOnly = true)
  public SettingsResponse getMySettings(UUID userId) {
    return toResponse(findOrThrow(userId));
  }

  @Transactional
  public SettingsResponse updateMySettings(UUID userId, UpdateSettingsRequest request) {
    UserSettings settings = findOrThrow(userId);
    if (request.theme() != null) {
      settings.setTheme(request.theme());
    }
    if (request.notificationPrefs() != null) {
      settings.setNotificationPrefs(request.notificationPrefs());
    }
    settings.setUpdatedAt(Instant.now());
    userSettingsRepository.save(settings);
    return toResponse(settings);
  }

  private UserSettings findOrThrow(UUID userId) {
    return userSettingsRepository
        .findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Settings not found."));
  }

  private SettingsResponse toResponse(UserSettings settings) {
    return new SettingsResponse(settings.getTheme(), settings.getNotificationPrefs());
  }
}
