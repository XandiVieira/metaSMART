package com.relyon.metasmart.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "metasmart.rate-limit")
@Getter
@Setter
public class RateLimitConfig {

    private boolean enabled = true;
    private AuthRateLimit auth = new AuthRateLimit();

    @Getter
    @Setter
    public static class AuthRateLimit {
        private int capacity = 10;
        private int refillTokens = 10;
        private int refillDurationSeconds = 60;
    }
}
