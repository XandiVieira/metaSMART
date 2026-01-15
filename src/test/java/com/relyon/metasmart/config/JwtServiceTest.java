package com.relyon.metasmart.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.relyon.metasmart.entity.user.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("password")
                .build();
    }

    @Nested
    @DisplayName("Token generation tests")
    class TokenGenerationTests {

        @Test
        @DisplayName("Should generate token successfully")
        void shouldGenerateToken() {
            String token = jwtService.generateToken(userDetails);

            assertThat(token).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("Should generate token with extra claims")
        void shouldGenerateTokenWithExtraClaims() {
            Map<String, Object> extraClaims = Map.of("role", "ADMIN", "userId", 1L);

            String token = jwtService.generateToken(extraClaims, userDetails);

            assertThat(token).isNotNull().isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Token extraction tests")
    class TokenExtractionTests {

        @Test
        @DisplayName("Should extract email from token")
        void shouldExtractEmailFromToken() {
            String token = jwtService.generateToken(userDetails);

            String email = jwtService.extractEmail(token);

            assertThat(email).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("Should throw exception for malformed token")
        void shouldThrowExceptionForMalformedToken() {
            assertThatThrownBy(() -> jwtService.extractEmail("invalid-token"))
                    .isInstanceOf(MalformedJwtException.class);
        }
    }

    @Nested
    @DisplayName("Token validation tests")
    class TokenValidationTests {

        @Test
        @DisplayName("Should validate token successfully")
        void shouldValidateTokenSuccessfully() {
            String token = jwtService.generateToken(userDetails);

            boolean isValid = jwtService.isTokenValid(token, userDetails);

            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Should return false for wrong user")
        void shouldReturnFalseForWrongUser() {
            String token = jwtService.generateToken(userDetails);

            UserDetails anotherUser = User.builder()
                    .id(2L)
                    .name("Jane Doe")
                    .email("jane@example.com")
                    .password("password")
                    .build();

            boolean isValid = jwtService.isTokenValid(token, anotherUser);

            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should return false for expired token")
        void shouldReturnFalseForExpiredToken() throws Exception {
            // Use reflection to temporarily set a very short expiration
            Field expirationField = JwtService.class.getDeclaredField("jwtExpiration");
            expirationField.setAccessible(true);
            long originalExpiration = (long) expirationField.get(jwtService);

            try {
                expirationField.set(jwtService, 1L); // 1 millisecond
                String token = jwtService.generateToken(userDetails);

                Thread.sleep(10); // Wait for token to expire

                assertThatThrownBy(() -> jwtService.isTokenValid(token, userDetails))
                        .isInstanceOf(ExpiredJwtException.class);
            } finally {
                expirationField.set(jwtService, originalExpiration);
            }
        }
    }
}
