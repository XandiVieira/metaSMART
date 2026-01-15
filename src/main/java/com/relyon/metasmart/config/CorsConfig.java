package com.relyon.metasmart.config;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@ConfigurationProperties(prefix = "metasmart.cors")
@Getter
@Setter
public class CorsConfig {

    private String allowedOrigins = "http://localhost:3000";
    private String allowedMethods = "GET,POST,PUT,PATCH,DELETE,OPTIONS";
    private String allowedHeaders = "Authorization,Content-Type,X-Requested-With";
    private boolean allowCredentials = true;
    private long maxAge = 3600;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(parseCommaSeparated(allowedOrigins));
        configuration.setAllowedMethods(parseCommaSeparated(allowedMethods));
        configuration.setAllowedHeaders(parseCommaSeparated(allowedHeaders));
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);
        configuration.setExposedHeaders(List.of("Authorization"));

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private List<String> parseCommaSeparated(String value) {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
