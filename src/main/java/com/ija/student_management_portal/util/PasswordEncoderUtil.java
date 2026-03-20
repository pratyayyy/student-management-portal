package com.ija.student_management_portal.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class for password encoding and management
 */
public class PasswordEncoderUtil {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * Encode a raw password using BCrypt
     * @param rawPassword the plain text password
     * @return the encoded password
     */
    public static String encodePassword(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * Verify if a raw password matches the encoded password
     * @param rawPassword the plain text password
     * @param encodedPassword the encoded password to verify against
     * @return true if passwords match, false otherwise
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }

    /**
     * Main method to generate encoded passwords for demo users
     * Usage: java PasswordEncoderUtil
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("=== BCrypt Password Encoder ===");
            System.out.println("Usage: java PasswordEncoderUtil <password>");
            System.out.println("\nExample demo passwords:");
            System.out.println("Admin password (password123): " + encodePassword("password123"));
            System.out.println("Student password (password123): " + encodePassword("password123"));
        } else {
            String password = args[0];
            System.out.println("Raw password: " + password);
            System.out.println("Encoded password: " + encodePassword(password));
        }
    }
}
