package com.rural.marketplace.user.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class to generate and verify BCrypt password hashes.
 * Run this main method to generate a hash for "password".
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String plainPassword = "password";
        String hashedPassword = encoder.encode(plainPassword);

        System.out.println("=== BCrypt Password Hash Generator ===");
        System.out.println("Plain Password: " + plainPassword);
        System.out.println("Hashed Password: " + hashedPassword);
        System.out.println();

        // Test the hash from V3 migration
        String dbHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        boolean matches = encoder.matches(plainPassword, dbHash);

        System.out.println("=== Verification ===");
        System.out.println("DB Hash: " + dbHash);
        System.out.println("Does 'password' match DB hash? " + matches);
        System.out.println();

        if (!matches) {
            System.out.println("⚠️ PASSWORD MISMATCH!");
            System.out.println("Run this SQL to fix:");
            System.out.println("UPDATE users SET password = '" + hashedPassword
                    + "' WHERE mobile_number IN ('9988776655', '9876543210', '1122334455');");
        } else {
            System.out.println("✅ Password hash is CORRECT!");
        }
    }
}
