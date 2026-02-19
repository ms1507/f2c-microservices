package com.rural.marketplace.user.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.rural.marketplace.common.security.JwtService;
import java.io.IOException;

/**
 * JWT Authentication Filter that intercepts HTTP requests to validate JWT
 * tokens.
 * This filter runs once per request and extracts the JWT from the Authorization
 * header.
 * If valid, it sets the authentication in the SecurityContext.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Main filter logic that processes each HTTP request.
     * Extracts JWT token from Authorization header, validates it, and sets
     * authentication.
     *
     * @param request     HTTP request
     * @param response    HTTP response
     * @param filterChain filter chain to continue processing
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String mobileNumber;

        // Step 1: Check if Authorization header exists and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No JWT token found in request to {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        // Step 2: Extract token (remove "Bearer " prefix)
        jwt = authHeader.substring(7);
        log.debug("JWT token found in request");

        try {
            // Step 3: Extract username (mobile number) from token
            mobileNumber = jwtService.extractUsername(jwt);
            log.debug("Extracted mobile number from token: {}", mobileNumber);

            // Step 4: Check if username exists and no authentication is set yet
            if (mobileNumber != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Step 5: Load user details from database
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(mobileNumber);

                // Step 6: Validate token against user details
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    log.info("Valid JWT token for user: {}", mobileNumber);

                    // Step 7: Create authentication token with user details and authorities
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    // Step 8: Set authentication in SecurityContext (user is now authenticated)
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Authentication set in SecurityContext for user: {}", mobileNumber);
                } else {
                    log.warn("Invalid JWT token for user: {}", mobileNumber);
                }
            }
        } catch (Exception e) {
            log.error("Error processing JWT token: {}", e.getMessage());
        }

        // Step 9: Continue filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Determines if this filter should be skipped for certain requests.
     * Skips JWT validation for authentication endpoints.
     *
     * @param request HTTP request
     * @return true if filter should be skipped
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auth/");
    }
}
