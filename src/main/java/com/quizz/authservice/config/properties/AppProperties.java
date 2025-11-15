package com.quizz.authservice.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * General application configuration properties.
 * Binds properties from application.yml with prefix 'app'
 */
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /**
     * Application name
     */
    private String name = "Quiz Auth Service";

    /**
     * Application version
     */
    private String version = "1.0.0";

    /**
     * Application environment (dev, staging, prod)
     */
    private String environment = "dev";

    /**
     * Security settings
     */
    private Security security = new Security();

    @Data
    public static class Security {
        /**
         * Minimum password length
         */
        private int minPasswordLength = 6;

        /**
         * Maximum login attempts before account lockout
         */
        private int maxLoginAttempts = 5;

        /**
         * Account lockout duration in minutes
         */
        private int lockoutDurationMinutes = 30;
    }
}
