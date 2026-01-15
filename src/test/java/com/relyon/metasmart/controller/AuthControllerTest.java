package com.relyon.metasmart.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.relyon.metasmart.config.CorsConfig;
import com.relyon.metasmart.config.JwtService;
import com.relyon.metasmart.config.RateLimitConfig;
import com.relyon.metasmart.config.SecurityConfig;
import com.relyon.metasmart.entity.user.dto.AuthResponse;
import com.relyon.metasmart.entity.user.dto.ForgotPasswordRequest;
import com.relyon.metasmart.entity.user.dto.LoginRequest;
import com.relyon.metasmart.entity.user.dto.RegisterRequest;
import com.relyon.metasmart.entity.user.dto.ResetPasswordRequest;
import com.relyon.metasmart.exception.AuthenticationException;
import com.relyon.metasmart.exception.BadRequestException;
import com.relyon.metasmart.exception.DuplicateResourceException;
import com.relyon.metasmart.exception.GlobalExceptionHandler;
import com.relyon.metasmart.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, CorsConfig.class, RateLimitConfig.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Nested
    @DisplayName("Register endpoint tests")
    class RegisterTests {

        @Test
        @DisplayName("Should register user successfully")
        void shouldRegisterUserSuccessfully() throws Exception {
            var request = RegisterRequest.builder()
                    .name("John Doe")
                    .email("john@example.com")
                    .password("Password123!")
                    .build();

            var response = AuthResponse.builder()
                    .token("jwt-token")
                    .email("john@example.com")
                    .name("John Doe")
                    .build();

            when(authService.register(any(RegisterRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.token").value("jwt-token"))
                    .andExpect(jsonPath("$.email").value("john@example.com"))
                    .andExpect(jsonPath("$.name").value("John Doe"));
        }

        @Test
        @DisplayName("Should return 409 when email already exists")
        void shouldReturn409WhenEmailExists() throws Exception {
            var request = RegisterRequest.builder()
                    .name("John Doe")
                    .email("john@example.com")
                    .password("Password123!")
                    .build();

            when(authService.register(any(RegisterRequest.class)))
                    .thenThrow(new DuplicateResourceException("Email already registered"));

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should return 400 for invalid request")
        void shouldReturn400ForInvalidRequest() throws Exception {
            var request = RegisterRequest.builder()
                    .name("")
                    .email("invalid-email")
                    .password("short")
                    .build();

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Login endpoint tests")
    class LoginTests {

        @Test
        @DisplayName("Should login user successfully")
        void shouldLoginUserSuccessfully() throws Exception {
            var request = LoginRequest.builder()
                    .email("john@example.com")
                    .password("password123")
                    .build();

            var response = AuthResponse.builder()
                    .token("jwt-token")
                    .email("john@example.com")
                    .name("John Doe")
                    .build();

            when(authService.login(any(LoginRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt-token"))
                    .andExpect(jsonPath("$.email").value("john@example.com"));
        }

        @Test
        @DisplayName("Should return 401 for invalid credentials")
        void shouldReturn401ForInvalidCredentials() throws Exception {
            var request = LoginRequest.builder()
                    .email("john@example.com")
                    .password("wrongpassword")
                    .build();

            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new AuthenticationException("Invalid email or password"));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Forgot password endpoint tests")
    class ForgotPasswordTests {

        @Test
        @DisplayName("Should request password reset successfully")
        void shouldRequestPasswordResetSuccessfully() throws Exception {
            var request = ForgotPasswordRequest.builder()
                    .email("john@example.com")
                    .build();

            doNothing().when(authService).forgotPassword(any(ForgotPasswordRequest.class));

            mockMvc.perform(post("/api/v1/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("If an account exists with this email, a password reset link has been sent"));

            verify(authService).forgotPassword(any(ForgotPasswordRequest.class));
        }

        @Test
        @DisplayName("Should return 200 even when email does not exist")
        void shouldReturn200EvenWhenEmailDoesNotExist() throws Exception {
            var request = ForgotPasswordRequest.builder()
                    .email("nonexistent@example.com")
                    .build();

            doNothing().when(authService).forgotPassword(any(ForgotPasswordRequest.class));

            mockMvc.perform(post("/api/v1/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").exists());
        }
    }

    @Nested
    @DisplayName("Reset password endpoint tests")
    class ResetPasswordTests {

        @Test
        @DisplayName("Should reset password successfully")
        void shouldResetPasswordSuccessfully() throws Exception {
            var request = ResetPasswordRequest.builder()
                    .token("valid-token")
                    .newPassword("NewPassword123!")
                    .build();

            doNothing().when(authService).resetPassword(any(ResetPasswordRequest.class));

            mockMvc.perform(post("/api/v1/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Password has been reset successfully"));

            verify(authService).resetPassword(any(ResetPasswordRequest.class));
        }
    }

    @Nested
    @DisplayName("Validate reset token endpoint tests")
    class ValidateResetTokenTests {

        @Test
        @DisplayName("Should return true for valid token")
        void shouldReturnTrueForValidToken() throws Exception {
            when(authService.validateResetToken("valid-token")).thenReturn(true);

            mockMvc.perform(get("/api/v1/auth/validate-reset-token")
                            .param("token", "valid-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valid").value(true));

            verify(authService).validateResetToken("valid-token");
        }

        @Test
        @DisplayName("Should return false for invalid token")
        void shouldReturnFalseForInvalidToken() throws Exception {
            when(authService.validateResetToken("invalid-token")).thenReturn(false);

            mockMvc.perform(get("/api/v1/auth/validate-reset-token")
                            .param("token", "invalid-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valid").value(false));
        }

        @Test
        @DisplayName("Should return false for expired token")
        void shouldReturnFalseForExpiredToken() throws Exception {
            when(authService.validateResetToken("expired-token")).thenReturn(false);

            mockMvc.perform(get("/api/v1/auth/validate-reset-token")
                            .param("token", "expired-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valid").value(false));
        }
    }
}
