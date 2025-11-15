package com.quizz.authservice.constant;

/**
 * Centralized error messages for consistent error handling.
 */
public final class ErrorMessages {

    private ErrorMessages() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Authentication errors
    public static final String INVALID_CREDENTIALS = "Invalid email or password";
    public static final String EMAIL_ALREADY_EXISTS = "User with this email already exists";
    public static final String INVALID_TOKEN = "Invalid or expired token";

    // User errors
    public static final String USER_NOT_FOUND = "User not found";
    public static final String USER_NOT_FOUND_WITH_ID = "User not found with id: %d";
    public static final String USER_NOT_FOUND_WITH_EMAIL = "User not found with email: %s";

    // Role errors
    public static final String ROLE_NOT_FOUND = "Role not found";
    public static final String DEFAULT_ROLE_NOT_FOUND = "Default role not found";

    // Validation errors
    public static final String EMAIL_REQUIRED = "Email is required";
    public static final String EMAIL_INVALID = "Email should be valid";
    public static final String PASSWORD_REQUIRED = "Password is required";
    public static final String PASSWORD_MIN_LENGTH = "Password must be at least %d characters";

    // General errors
    public static final String UNEXPECTED_ERROR = "An unexpected error occurred";
    public static final String OPERATION_FAILED = "Operation failed";

    /**
     * Format error message with parameters
     */
    public static String format(String template, Object... args) {
        return String.format(template, args);
    }
}
