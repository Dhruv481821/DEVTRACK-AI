package com.devtrack.common.exception;

/**
 * 404. Also thrown by the shared assertOwnership() helper (AuthorizationHelper) for
 * a resource that exists but isn't owned by the requester — deliberately never a
 * separate "not owned" exception, per /docs/06_API_Specification.md §1.6: returning
 * 403 would confirm the resource exists to someone who shouldn't know that.
 */
public class ResourceNotFoundException extends DevTrackException {
    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message);
    }
}
