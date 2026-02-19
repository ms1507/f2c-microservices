package com.rural.marketplace.user.service.impl;

import com.rural.marketplace.user.dto.RegisterRequest;
import com.rural.marketplace.user.dto.UserResponse;
import com.rural.marketplace.user.dto.UserUpdateRequest;
import com.rural.marketplace.user.entity.Role;
import com.rural.marketplace.user.entity.User;
import com.rural.marketplace.user.repository.RoleRepository;
import com.rural.marketplace.user.repository.UserRepository;
import com.rural.marketplace.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.rural.marketplace.common.exception.ResourceNotFoundException;
import com.rural.marketplace.common.exception.ConflictException;

/**
 * Service implementation for user management operations.
 * Handles user registration, profile updates, and soft deletion.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new user in the system.
     * Validates mobile number uniqueness, encodes password, and assigns role.
     *
     * @param request registration details
     * @return UserResponse containing registered user information
     * @throws RuntimeException if mobile number already exists or role not found
     */
    @Override
    @Transactional
    public UserResponse registerUser(RegisterRequest request) {
        log.info("Registering user with mobile: {}", request.getMobileNumber());

        // Step 1: Check for duplicate mobile number
        if (userRepository.existsByMobileNumber(request.getMobileNumber())) {
            log.error("Registration failed: Mobile number already exists: {}", request.getMobileNumber());
            throw new ConflictException(
                    "User with this mobile number already exists");
        }

        // Step 2: Fetch Role from database
        Role userRole = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> {
                    log.error("Role not found: {}", request.getRole());
                    return new ResourceNotFoundException(
                            "Role not found: " + request.getRole());
                });
        log.debug("Assigned role: {} to user", userRole.getName());

        // Step 3: Build User entity with encoded password
        User user = User.builder()
                .fullName(request.getFullName())
                .mobileNumber(request.getMobileNumber())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // Password encryption
                .village(request.getVillage())
                .district(request.getDistrict())
                .state(request.getState())
                .pincode(request.getPincode())
                .isActive(true)
                .isVerified(false) // Will be verified later via OTP
                .build();

        // Step 4: Assign role to user
        user.getRoles().add(userRole);

        // Step 5: Save user to database
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        // Step 6: Convert to response DTO and return
        return mapToResponse(savedUser);
    }

    /**
     * Retrieves user by ID.
     *
     * @param id user ID
     * @return UserResponse
     * @throws RuntimeException if user not found
     */
    @Override
    public UserResponse getUserById(Long id) {
        log.debug("Fetching user by ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", id);
                    return new ResourceNotFoundException(
                            "User not found with id: " + id);
                });
        return mapToResponse(user);
    }

    /**
     * Updates user profile information.
     * Only updates fields that are non-null in the request.
     *
     * @param id      user ID
     * @param request update request with fields to modify
     * @return updated UserResponse
     * @throws RuntimeException if user not found
     */
    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        log.info("Updating user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", id);
                    return new ResourceNotFoundException(
                            "User not found with id: " + id);
                });

        // Update only non-null fields
        if (request.getFullName() != null)
            user.setFullName(request.getFullName());
        if (request.getEmail() != null)
            user.setEmail(request.getEmail());
        if (request.getVillage() != null)
            user.setVillage(request.getVillage());
        if (request.getDistrict() != null)
            user.setDistrict(request.getDistrict());
        if (request.getState() != null)
            user.setState(request.getState());
        if (request.getPincode() != null)
            user.setPincode(request.getPincode());
        if (request.getAddress() != null)
            user.setAddress(request.getAddress());
        if (request.getPreferredLanguage() != null)
            user.setPreferredLanguage(request.getPreferredLanguage());

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", id);
        return mapToResponse(updatedUser);
    }

    /**
     * Soft deletes a user by setting is_active to false.
     * User data is retained in the database.
     *
     * @param id user ID to delete
     * @throws RuntimeException if user not found
     */
    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.warn("Soft deleting user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", id);
                    return new ResourceNotFoundException(
                            "User not found with id: " + id);
                });
        user.setActive(false);
        userRepository.save(user);
        log.info("User soft deleted: {}", id);
    }

    /**
     * Maps User entity to UserResponse DTO.
     *
     * @param user User entity
     * @return UserResponse DTO
     */
    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .mobileNumber(user.getMobileNumber())
                .role(user.getRoles().stream().findFirst().map(Role::getName).orElse("UNKNOWN"))
                .isActive(user.isActive())
                .build();
    }
}
