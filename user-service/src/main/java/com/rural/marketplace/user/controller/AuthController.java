package com.rural.marketplace.user.controller;

import com.rural.marketplace.user.dto.AuthResponse;
import com.rural.marketplace.user.dto.LoginRequest;
import com.rural.marketplace.user.dto.RegisterRequest;
import com.rural.marketplace.user.dto.UserResponse;
import com.rural.marketplace.user.entity.User;
import com.rural.marketplace.user.repository.UserRepository;
import com.rural.marketplace.common.security.JwtService;
import com.rural.marketplace.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for authentication operations.
 * Handles user registration and login endpoints.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    /**
     * Registers a new user in the system.
     *
     * @param request registration details (mobile, password, role, etc.)
     * @return UserResponse containing registered user information
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        log.info("Registration request received for mobile: {}", request.getMobileNumber());
        UserResponse response = userService.registerUser(request);
        log.info("User registered successfully with ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param request login credentials (mobile number and password)
     * @return AuthResponse containing JWT token and user details
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        log.info("Login attempt for mobile: {}", request.getMobileNumber());

        try {
            // Step 1: Authenticate user credentials using Spring Security
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getMobileNumber(),
                            request.getPassword()));
            log.debug("Authentication successful for mobile: {}", request.getMobileNumber());

            // Step 2: Load user details from database
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getMobileNumber());

            // Step 3: Fetch user entity for additional response data and JWT claims
            User user = userRepository.findByMobileNumber(request.getMobileNumber())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Step 4: Generate JWT token with userId claim for authenticated user
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("userId", user.getId());
            String jwtToken = jwtService.generateToken(extraClaims, userDetails);
            log.info("JWT token generated for user: {} (ID: {})", request.getMobileNumber(), user.getId());

            // Step 5: Build and return authentication response with token
            return ResponseEntity.ok(AuthResponse.builder()
                    .token(jwtToken)
                    .mobileNumber(user.getMobileNumber())
                    .fullName(user.getFullName())
                    .role(user.getRoles().stream().findFirst().map(role -> role.getName()).orElse("UNKNOWN"))
                    .build());
        } catch (Exception e) {
            log.error("Login failed for mobile: {}. Error: {}", request.getMobileNumber(), e.getMessage(), e);
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }

    /**
     * Test endpoint to verify auth service is running.
     *
     * @return simple status message
     */
    @GetMapping("/test")
    public String test() {
        log.debug("Test endpoint accessed");
        return "Auth Service Working";
    }

    /**
     * Validates a JWT token.
     *
     * @param token JWT token string
     * @return boolean indicating validity
     */
    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestParam("token") String token) {
        log.info("Token validation request received");
        boolean isValid = jwtService.validateToken(token);
        return ResponseEntity.ok(isValid);
    }
}
