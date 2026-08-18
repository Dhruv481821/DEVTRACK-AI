package com.devtrack.profile.service;

import com.devtrack.common.events.UserRegisteredEvent;
import com.devtrack.profile.entity.UserProfile;
import com.devtrack.profile.entity.UserSettings;
import com.devtrack.profile.repository.UserProfileRepository;
import com.devtrack.profile.repository.UserSettingsRepository;
import java.time.Instant;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * First real listener on the domain event bus (04_System_Architecture.md §3.5/§5) — creates the
 * default UserProfile and UserSettings rows the moment a user is created, without AuthService
 * knowing this module exists. Fixes the gap where GET /profile/me or /settings/me would 404 for
 * every new user, since registration itself never created these rows.
 *
 * <p><strong>Deliberately synchronous, not @TransactionalEventListener(AFTER_COMMIT):</strong>
 * plain @EventListener runs in the same transaction as AuthService.register(), which means this
 * table write is part of the same all-or-nothing commit — if profile creation somehow fails,
 * registration fails too, rather than leaving a user with no profile row. That atomicity matters
 * more here than decoupling the transaction boundaries would: "every AppUser has a UserProfile" is
 * a real invariant this code guarantees, not just an expectation.
 */
@Component
public class UserProfileInitializer {

  private final UserProfileRepository userProfileRepository;
  private final UserSettingsRepository userSettingsRepository;

  public UserProfileInitializer(
      UserProfileRepository userProfileRepository, UserSettingsRepository userSettingsRepository) {
    this.userProfileRepository = userProfileRepository;
    this.userSettingsRepository = userSettingsRepository;
  }

  @EventListener
  @Transactional
  public void onUserRegistered(UserRegisteredEvent event) {
    UserProfile profile = new UserProfile();
    profile.setUserId(event.userId());
    profile.setUpdatedAt(Instant.now());
    userProfileRepository.save(profile);

    UserSettings settings = new UserSettings();
    settings.setUserId(event.userId());
    settings.setUpdatedAt(Instant.now());
    userSettingsRepository.save(settings);
  }
}
