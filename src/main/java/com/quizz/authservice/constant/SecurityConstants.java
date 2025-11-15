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

    /**
     * Cookie-related constants
     */
    public static final class Cookie {

        private Cookie() {
            throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
        }

        /**
         * Set-Cookie header name
         */
        public static final String SET_COOKIE_HEADER = "Set-Cookie";

        /**
         * Cookie format template for setting authentication cookie
         */
        public static final String AUTH_COOKIE_FORMAT = "%s=%s; HttpOnly; %sPath=%s; Max-Age=%d; SameSite=%s";

        /**
         * Cookie format template for clearing authentication cookie
         */
        public static final String CLEAR_COOKIE_FORMAT = "%s=; HttpOnly; %sPath=%s; Max-Age=0; SameSite=%s";

        /**
         * Secure flag for production cookies
         */
        public static final String SECURE_FLAG = "Secure; ";

        /**
         * Empty string for non-secure cookies (development)
         */
        public static final String NO_SECURE_FLAG = "";
    }
}
