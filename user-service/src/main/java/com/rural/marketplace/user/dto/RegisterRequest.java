package com.rural.marketplace.user.dto;

import com.rural.marketplace.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String fullName;
    private String mobileNumber;
    private String email;
    private String password;
    private String village;
    private String district;
    private String state;
    private String pincode;
    private String role; // String input (FARMER, BUYER) to try matching DB roles
}
