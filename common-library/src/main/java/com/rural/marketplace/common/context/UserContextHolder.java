package com.rural.marketplace.common.context;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class to extract user context information from HTTP request headers.
 * The API Gateway injects user information as headers after JWT validation.
 * This allows downstream microservices to access user context without JWT
 * parsing.
 */
@Slf4j
public class UserContextHolder {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USERNAME_HEADER = "X-Logged-In-User";

    /**
     * Extracts the current logged-in user ID from the request header.
     * The API Gateway sets this header after validating the JWT token.
     *
     * @param request HTTP servlet request
     * @return user ID as Long, or null if not present
     */
    public static Long getCurrentUserId(HttpServletRequest request) {
        String userIdHeader = request.getHeader(USER_ID_HEADER);
        if (userIdHeader != null && !userIdHeader.isEmpty()) {
            try {
                return Long.parseLong(userIdHeader);
            } catch (NumberFormatException e) {
                log.error("Invalid user ID format in header: {}", userIdHeader);
                return null;
            }
        }
        log.debug("No user ID found in request headers");
        return null;
    }

    /**
     * Extracts the current logged-in username (mobile number) from the request
     * header.
     * The API Gateway sets this header after validating the JWT token.
     *
     * @param request HTTP servlet request
     * @return username as String, or null if not present
     */
    public static String getCurrentUsername(HttpServletRequest request) {
        String username = request.getHeader(USERNAME_HEADER);
        if (username == null || username.isEmpty()) {
            log.debug("No username found in request headers");
            return null;
        }
        return username;
    }

    /**
     * Checks if the current request has authenticated user context.
     *
     * @param request HTTP servlet request
     * @return true if user context is present, false otherwise
     */
    public static boolean hasUserContext(HttpServletRequest request) {
        return getCurrentUserId(request) != null;
    }
}
