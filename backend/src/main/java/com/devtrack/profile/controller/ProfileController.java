package com.devtrack.profile.controller;

import com.devtrack.common.dto.ApiEnvelope;
import com.devtrack.common.security.CurrentUserResolver;
import com.devtrack.profile.dto.request.UpdateProfileRequest;
import com.devtrack.profile.dto.response.ProfileResponse;
import com.devtrack.profile.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** FR-PROF-01, per /docs/06_API_Specification.md §2.2. */
@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

  private final ProfileService profileService;
  private final CurrentUserResolver currentUserResolver;

  public ProfileController(ProfileService profileService, CurrentUserResolver currentUserResolver) {
    this.profileService = profileService;
    this.currentUserResolver = currentUserResolver;
  }

  @GetMapping("/me")
  public ApiEnvelope<ProfileResponse> getMyProfile() {
    return ApiEnvelope.success(profileService.getMyProfile(currentUserResolver.getCurrentUserId()));
  }

  @PatchMapping("/me")
  public ApiEnvelope<ProfileResponse> updateMyProfile(
      @Valid @RequestBody UpdateProfileRequest request) {
    return ApiEnvelope.success(
        profileService.updateMyProfile(currentUserResolver.getCurrentUserId(), request));
  }

  // POST /me/avatar (Cloudinary upload) — deliberately not built in this slice.
  // Multipart file handling + a real external API integration is a meaningfully
  // separate piece of work from these two mechanical CRUD endpoints; see
  // /docs/06_API_Specification.md §2.2 for the endpoint this will eventually be.
}
