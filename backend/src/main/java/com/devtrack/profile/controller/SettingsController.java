package com.devtrack.profile.controller;

import com.devtrack.common.dto.ApiEnvelope;
import com.devtrack.common.security.CurrentUserResolver;
import com.devtrack.profile.dto.request.UpdateSettingsRequest;
import com.devtrack.profile.dto.response.SettingsResponse;
import com.devtrack.profile.service.SettingsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** FR-SET-01, per /docs/06_API_Specification.md §2.2. */
@RestController
@RequestMapping("/api/v1/settings")
public class SettingsController {

    private final SettingsService settingsService;
    private final CurrentUserResolver currentUserResolver;

    public SettingsController(SettingsService settingsService, CurrentUserResolver currentUserResolver) {
        this.settingsService = settingsService;
        this.currentUserResolver = currentUserResolver;
    }

    @GetMapping("/me")
    public ApiEnvelope<SettingsResponse> getMySettings() {
        return ApiEnvelope.success(settingsService.getMySettings(currentUserResolver.getCurrentUserId()));
    }

    @PatchMapping("/me")
    public ApiEnvelope<SettingsResponse> updateMySettings(@Valid @RequestBody UpdateSettingsRequest request) {
        return ApiEnvelope.success(
                settingsService.updateMySettings(currentUserResolver.getCurrentUserId(), request));
    }
}
