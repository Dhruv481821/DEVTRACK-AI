package com.devtrack.common.security;

import com.devtrack.common.dto.ApiEnvelope;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Spring Security's default 401/403 responses aren't JSON and don't match the
 * envelope every other endpoint uses (06_API_Specification.md §1.3). These two
 * classes are the only place a 401/403 originates from the security filter chain
 * itself (as opposed to GlobalExceptionHandler, which handles them once a request
 * has reached a controller) — kept consistent with the same envelope shape either way.
 */
@Component
class JsonAuthEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
            throws IOException {
        writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "AUTH_INVALID_CREDENTIALS", "Authentication required.");
    }

    void writeError(HttpServletResponse response, int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(mapper.writeValueAsString(ApiEnvelope.error(code, message, null)));
    }
}

@Component
class JsonAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(mapper.writeValueAsString(ApiEnvelope.error("FORBIDDEN", "Access denied.", null)));
    }
}
