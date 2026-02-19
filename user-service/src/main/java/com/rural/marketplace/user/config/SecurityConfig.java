package com.rural.marketplace.user.config;

import com.rural.marketplace.user.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security Configuration for the User Service.
 * Configures JWT-based stateless authentication with method-level security.
 */
@Configuration // Marks this class as a source of bean definitions
@EnableWebSecurity // Enables Spring Security's web security support
@EnableMethodSecurity // Enables @PreAuthorize, @PostAuthorize annotations on methods
@RequiredArgsConstructor // Lombok: generates constructor for final fields
public class SecurityConfig {

    // Injected via constructor (Lombok @RequiredArgsConstructor)
    private final JwtAuthenticationFilter jwtAuthFilter; // Custom filter to validate JWT tokens
    private final UserDetailsService userDetailsService; // Service to load user from database

    /**
     * Configures the security filter chain.
     * This defines HOW requests are secured.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Step 1: Disable CSRF (Cross-Site Request Forgery) protection
                // Why? REST APIs are stateless and use tokens, not cookies
                .csrf(AbstractHttpConfigurer::disable)

                // Step 2: Configure URL-based authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Allow public access to /api/v1/auth/** (login, register)
                        // Internally: Spring Security skips authentication for these URLs
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // All other requests require authentication
                        // Internally: Spring Security checks if SecurityContext has an Authentication
                        // object
                        .anyRequest().authenticated())

                // Step 3: Configure session management
                .sessionManagement(session -> session
                        // Set session policy to STATELESS
                        // Internally: Spring Security will NOT create or use HTTP sessions
                        // Each request must contain a JWT token for authentication
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Step 4: Disable form-based login
                // Internally: Removes the UsernamePasswordAuthenticationFilter that redirects
                // to /login
                .formLogin(AbstractHttpConfigurer::disable)

                // Step 5: Disable HTTP Basic authentication
                // Internally: Removes the BasicAuthenticationFilter that prompts for
                // username/password
                .httpBasic(AbstractHttpConfigurer::disable)

                // Step 6: Register our custom authentication provider
                // Internally: Spring Security uses this to validate credentials during login
                .authenticationProvider(authenticationProvider())

                // Step 7: Add our JWT filter BEFORE the default authentication filter
                // Internally: For each request, JwtAuthenticationFilter runs first to extract
                // and validate JWT
                // If valid, it sets the Authentication in SecurityContext before other filters
                // run
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // Return the configured SecurityFilterChain
        return http.build();
    }

    /**
     * Creates an authentication provider that uses our UserDetailsService and
     * password encoder.
     * Internally: This is used by AuthenticationManager to validate login
     * credentials.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        // Create a DAO-based authentication provider
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        // Set the UserDetailsService to load user from database
        // Internally: When login happens, this service fetches user by username (mobile
        // number)
        authProvider.setUserDetailsService(userDetailsService);

        // Set the password encoder to BCrypt
        // Internally: When comparing passwords, this encoder's matches() method is
        // called
        // It extracts salt from stored hash and compares with incoming password
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    /**
     * Exposes the AuthenticationManager as a bean.
     * Internally: This is used in AuthController to authenticate login requests.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        // Get the default AuthenticationManager from Spring Security
        // Internally: It uses the authenticationProvider() we configured above
        return config.getAuthenticationManager();
    }

    /**
     * Creates the password encoder bean.
     * Internally: BCrypt hashes passwords with a random salt and cost factor of 10
     * (2^10 iterations).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCryptPasswordEncoder with default strength (10)
        // Internally:
        // - encode("password") → generates hash with random salt
        // - matches("password", hash) → extracts salt from hash and compares
        return new BCryptPasswordEncoder();
    }
}
