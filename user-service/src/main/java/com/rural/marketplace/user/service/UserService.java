package com.rural.marketplace.user.service;

import com.rural.marketplace.user.dto.RegisterRequest;
import com.rural.marketplace.user.dto.UserResponse;
import com.rural.marketplace.user.dto.UserUpdateRequest;

public interface UserService {
    UserResponse registerUser(RegisterRequest request);

    UserResponse getUserById(Long id);

    UserResponse updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long id);
}
