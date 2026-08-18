package com.devtrack.common.security;

import com.devtrack.common.exception.ResourceNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Implements the 404-not-403 ownership rule from /docs/06_API_Specification.md §1.6
 * and /docs/07_Backend_Architecture.md §4: a resource that exists but isn't owned by
 * the requesting user returns 404, never 403 — 403 would confirm the resource
 * exists to someone who shouldn't know that. Every module with per-user-owned
 * resources calls this one helper rather than writing its own ownership check, so
 * the rule can't be accidentally violated by a future module reaching for a more
 * "accurate"-sounding 403 exception instead.
 */
@Component
public class OwnershipGuard {

    public void assertOwnedBy(UUID resourceOwnerId, UUID requestingUserId) {
        if (!resourceOwnerId.equals(requestingUserId)) {
            throw new ResourceNotFoundException("Resource not found.");
        }
    }
}
