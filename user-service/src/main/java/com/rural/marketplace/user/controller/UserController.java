package com.rural.marketplace.user.controller;

import com.rural.marketplace.user.dto.UserResponse;
import com.rural.marketplace.user.dto.UserUpdateRequest;
import com.rural.marketplace.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for user management operations.
 * Handles CRUD operations for user profiles.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * Retrieves user profile by ID.
     *
     * @param id user ID
     * @return UserResponse containing user details
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        log.info("Fetching user profile for ID: {}", id);
        UserResponse response = userService.getUserById(id);
        log.debug("User profile retrieved for ID: {}", id);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates user profile information.
     *
     * @param id      user ID
     * @param request update request containing fields to modify
     * @return updated UserResponse
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        log.info("Update request received for user ID: {}", id);
        UserResponse response = userService.updateUser(id, request);
        log.info("User profile updated successfully for ID: {}", id);
        return ResponseEntity.ok(response);
    }

    /**
     * Soft deletes a user (sets is_active to false).
     *
     * @param id user ID to delete
     * @return no content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.warn("Delete request received for user ID: {}", id);
        userService.deleteUser(id);
        log.info("User soft deleted successfully: {}", id);
        return ResponseEntity.noContent().build();
    }
}
