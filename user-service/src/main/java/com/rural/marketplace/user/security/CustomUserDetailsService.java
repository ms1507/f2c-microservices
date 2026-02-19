package com.rural.marketplace.user.security;

import com.rural.marketplace.user.entity.Role;
import com.rural.marketplace.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Custom implementation of Spring Security's UserDetailsService.
 * Loads user-specific data from the database for authentication.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads user by mobile number (used as username in our system).
     * This method is called by Spring Security during authentication.
     *
     * @param mobileNumber user's mobile number
     * @return UserDetails object containing user information and authorities
     * @throws UsernameNotFoundException if user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String mobileNumber) throws UsernameNotFoundException {
        log.debug("Loading user by mobile number: {}", mobileNumber);

        // Fetch user from database
        com.rural.marketplace.user.entity.User user = userRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> {
                    log.error("User not found with mobile number: {}", mobileNumber);
                    return new UsernameNotFoundException("User not found with mobile: " + mobileNumber);
                });

        log.info("User found: {} with roles: {}", user.getMobileNumber(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.joining(", ")));

        // Build Spring Security UserDetails object
        return User.builder()
                .username(user.getMobileNumber())
                .password(user.getPassword())
                .authorities(getAuthorities(user.getRoles()))
                .accountExpired(false)
                .accountLocked(!user.isActive()) // Lock account if user is inactive
                .credentialsExpired(false)
                .disabled(!user.isActive()) // Disable if user is inactive
                .build();
    }

    /**
     * Converts user roles to Spring Security GrantedAuthority objects.
     * Adds "ROLE_" prefix as required by Spring Security.
     *
     * @param roles collection of user roles
     * @return collection of GrantedAuthority objects
     */
    private Collection<? extends GrantedAuthority> getAuthorities(Collection<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());
    }
}
