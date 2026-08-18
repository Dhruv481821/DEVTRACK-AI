package com.devtrack.common.security;

import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * JwtAuthenticationFilter sets the JWT's subject claim (the user's ID) as the Authentication
 * principal — this is the one place that detail is known, so every "me" endpoint calls this instead
 * of re-deriving it from SecurityContextHolder directly.
 */
@Component
public class CurrentUserResolver {

  public UUID getCurrentUserId() {
    String principal =
        (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return UUID.fromString(principal);
  }
}
