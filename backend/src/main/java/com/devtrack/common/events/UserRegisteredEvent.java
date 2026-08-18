package com.devtrack.common.events;

import java.util.UUID;

/**
 * Published by AuthService whenever a genuinely new user is created (password
 * registration or first-time Google login) — never on an existing user logging in
 * again. Listeners (profile module's UserProfileInitializer, and eventually
 * Notifications/Calendar per 04_System_Architecture.md §5) react without AuthService
 * knowing or caring who's listening — this is the first real exercise of the event
 * bus pattern designed in that document, not just a diagram until now.
 */
public record UserRegisteredEvent(UUID userId) {}
