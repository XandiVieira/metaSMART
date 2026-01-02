package com.relyon.metasmart.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "stripe")
public class StripeConfig {

    private String apiKey;
    private String webhookSecret;
    private Prices prices = new Prices();

    @PostConstruct
    public void init() {
        if (apiKey != null && !apiKey.isBlank()) {
            Stripe.apiKey = apiKey;
        }
    }

    @Getter
    @Setter
    public static class Prices {
        private String premiumMonthly;
        private String premiumYearly;
        private String streakShield;
        private String strugglingAssist;
        private String goalBoost;
        private String guardianSlot;
    }
}
