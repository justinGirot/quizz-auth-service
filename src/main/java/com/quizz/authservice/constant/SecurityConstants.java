package com.quizz.authservice.constant;

/**
 * Security-related constants used throughout the application.
 */
public final class SecurityConstants {

    private SecurityConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * JWT token prefix in Authorization header
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * Authorization header name
     */
    public static final String HEADER_STRING = "Authorization";

    /**
     * Default token expiration time (24 hours in milliseconds)
     */
    public static final long DEFAULT_EXPIRATION_TIME = 86_400_000L;
}
