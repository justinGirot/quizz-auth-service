package com.quizz.authservice.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Configuration properties for JWT token management.
 * Binds properties from application.yml with prefix 'jwt'
 */
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * Secret key used for signing JWT tokens.
     * Should be at least 256 bits (32 bytes) for HS256 algorithm.
     * Configure via environment variable in production.
     */
    @NotBlank(message = "JWT secret must not be blank")
    private String secret;

    /**
     * Token expiration time in milliseconds.
     * Default: 86400000 (24 hours)
     */
    @Positive(message = "JWT expiration must be positive")
    private long expiration = 86400000L;

    /**
     * Token issuer identifier
     */
    private String issuer = "quiz-auth-service";

    /**
     * Token audience (intended recipient)
     */
    private String audience = "quiz-application";
}
