package com.quizz.authservice.util;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

/**
 * Utility class for validation operations.
 */
@UtilityClass
public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    /**
     * Validate email format
     * @param email Email to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate password strength
     * @param password Password to validate
     * @param minLength Minimum required length
     * @return true if valid, false otherwise
     */
    public static boolean isValidPassword(String password, int minLength) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        return password.length() >= minLength;
    }

    /**
     * Check if string is null or empty
     * @param str String to check
     * @return true if null or empty, false otherwise
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
