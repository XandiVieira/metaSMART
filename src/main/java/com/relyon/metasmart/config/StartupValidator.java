package com.relyon.metasmart.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Profile("prod")
public class StartupValidator {

    @Value("${DB_URL:}")
    private String dbUrl;

    @Value("${DB_USERNAME:}")
    private String dbUsername;

    @Value("${DB_PASSWORD:}")
    private String dbPassword;

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${metasmart.cors.allowed-origins:}")
    private String corsOrigins;

    @Value("${metasmart.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Value("${stripe.api-key:}")
    private String stripeApiKey;

    @Value("${stripe.webhook-secret:}")
    private String stripeWebhookSecret;

    @EventListener(ApplicationReadyEvent.class)
    public void validateConfiguration() {
        log.info("Validating production configuration...");

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Required validations
        if (isBlank(dbUrl)) {
            errors.add("DB_URL is not set");
        }
        if (isBlank(dbUsername)) {
            errors.add("DB_USERNAME is not set");
        }
        if (isBlank(dbPassword)) {
            errors.add("DB_PASSWORD is not set");
        }
        if (isBlank(jwtSecret)) {
            errors.add("JWT_SECRET is not set");
        } else if (jwtSecret.length() < 32) {
            errors.add("JWT_SECRET must be at least 32 characters (256 bits)");
        }
        if (isBlank(corsOrigins) || corsOrigins.contains("localhost")) {
            warnings.add("CORS_ALLOWED_ORIGINS contains localhost or is not set - verify this is intentional");
        }

        // Email validation (if enabled)
        if (mailEnabled) {
            if (isBlank(mailUsername)) {
                errors.add("MAIL_USERNAME is required when MAIL_ENABLED=true");
            }
            if (isBlank(mailPassword)) {
                errors.add("MAIL_PASSWORD is required when MAIL_ENABLED=true");
            }
        } else {
            warnings.add("Email is disabled (MAIL_ENABLED=false) - password reset and notifications will not work");
        }

        // Stripe validation (warn if not configured)
        if (isBlank(stripeApiKey)) {
            warnings.add("STRIPE_API_KEY is not set - payment features will not work");
        }
        if (isBlank(stripeWebhookSecret)) {
            warnings.add("STRIPE_WEBHOOK_SECRET is not set - Stripe webhooks will not be validated");
        }

        // Log warnings
        warnings.forEach(warning -> log.warn("Configuration warning: {}", warning));

        // Fail if there are errors
        if (!errors.isEmpty()) {
            errors.forEach(error -> log.error("Configuration error: {}", error));
            throw new IllegalStateException(
                    "Application cannot start due to configuration errors: " + String.join(", ", errors)
            );
        }

        log.info("Production configuration validated successfully");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
