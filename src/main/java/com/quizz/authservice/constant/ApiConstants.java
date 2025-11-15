package com.quizz.authservice.constant;

/**
 * API-related constants.
 */
public final class ApiConstants {

    private ApiConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Base API path
     */
    public static final String API_BASE_PATH = "/api";

    /**
     * Authentication endpoints base path
     */
    public static final String AUTH_BASE_PATH = API_BASE_PATH + "/auth";

    /**
     * User endpoints base path
     */
    public static final String USERS_BASE_PATH = API_BASE_PATH + "/users";

    /**
     * Public endpoints (no authentication required)
     */
    public static final String[] PUBLIC_URLS = {
            AUTH_BASE_PATH + "/**",
            "/actuator/**",
            "/h2-console/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/api-docs/**"
    };
}
