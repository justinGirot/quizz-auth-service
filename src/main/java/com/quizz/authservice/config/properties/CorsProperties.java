package com.quizz.authservice.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for CORS (Cross-Origin Resource Sharing).
 * Binds properties from application.yml with prefix 'cors'
 */
@Data
@Component
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {

    /**
     * List of allowed origins for CORS requests.
     * Example: http://localhost:5173, http://localhost:8080
     */
    private List<String> allowedOrigins = new ArrayList<>();

    /**
     * List of allowed HTTP methods.
     * Default: GET, POST, PUT, DELETE, OPTIONS
     */
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");

    /**
     * List of allowed headers.
     * Default: * (all headers)
     */
    private List<String> allowedHeaders = List.of("*");

    /**
     * Whether to allow credentials (cookies, authorization headers).
     * Default: true
     */
    private boolean allowCredentials = true;

    /**
     * Maximum age (in seconds) for preflight request caching.
     * Default: 3600 (1 hour)
     */
    private long maxAge = 3600L;
}
