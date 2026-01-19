package com.relyon.metasmart.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "cloudinary")
public class CloudinaryConfig {

    private String cloudName;
    private String apiKey;
    private String apiSecret;
    private String folder;

    @PostConstruct
    public void init() {
        if (isConfigured()) {
            log.info("Cloudinary configured with cloud name: {}", cloudName);
        } else {
            log.warn("Cloudinary is not configured. Profile picture uploads will be disabled.");
        }
    }

    @Bean
    public Cloudinary cloudinary() {
        if (!isConfigured()) {
            return null;
        }

        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    public boolean isConfigured() {
        return cloudName != null && !cloudName.isBlank()
                && apiKey != null && !apiKey.isBlank()
                && apiSecret != null && !apiSecret.isBlank();
    }
}
