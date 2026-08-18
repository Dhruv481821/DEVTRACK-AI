package com.devtrack.profile.service;

import com.devtrack.common.exception.ResourceNotFoundException;
import com.devtrack.profile.dto.request.UpdateProfileRequest;
import com.devtrack.profile.dto.response.ProfileResponse;
import com.devtrack.profile.entity.UserProfile;
import com.devtrack.profile.repository.UserProfileRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** FR-PROF-01. */
@Service
public class ProfileService {

  private final UserProfileRepository userProfileRepository;

  public ProfileService(UserProfileRepository userProfileRepository) {
    this.userProfileRepository = userProfileRepository;
  }

  @Transactional(readOnly = true)
  public ProfileResponse getMyProfile(UUID userId) {
    UserProfile profile = findOrThrow(userId);
    return toResponse(profile);
  }

  @Transactional
  public ProfileResponse updateMyProfile(UUID userId, UpdateProfileRequest request) {
    UserProfile profile = findOrThrow(userId);
    // PATCH semantics — only overwrite fields the caller actually provided.
    if (request.displayName() != null) {
      profile.setDisplayName(request.displayName());
    }
    if (request.bio() != null) {
      profile.setBio(request.bio());
    }
    profile.setUpdatedAt(Instant.now());
    userProfileRepository.save(profile);
    return toResponse(profile);
  }

  private UserProfile findOrThrow(UUID userId) {
    // Should always exist — created by UserProfileInitializer at registration —
    // so hitting this exception means a real invariant was violated, not a
    // normal "not found" case a client caused.
    return userProfileRepository
        .findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Profile not found."));
  }

  private ProfileResponse toResponse(UserProfile profile) {
    return new ProfileResponse(profile.getDisplayName(), profile.getAvatarUrl(), profile.getBio());
  }
}
