package com.rural.marketplace.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    private String fullName;
    private String email;
    private String village;
    private String district;
    private String state;
    private String pincode;
    private String address;
    private String preferredLanguage;
}
