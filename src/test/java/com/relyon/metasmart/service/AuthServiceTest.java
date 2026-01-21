package com.relyon.metasmart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.relyon.metasmart.config.JwtService;
import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.entity.user.PasswordResetToken;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.entity.user.dto.ForgotPasswordRequest;
import com.relyon.metasmart.entity.user.dto.LoginRequest;
import com.relyon.metasmart.entity.user.dto.RegisterRequest;
import com.relyon.metasmart.entity.user.dto.ResetPasswordRequest;
import com.relyon.metasmart.exception.AuthenticationException;
import com.relyon.metasmart.exception.BadRequestException;
import com.relyon.metasmart.exception.DuplicateResourceException;
import com.relyon.metasmart.mapper.AuthMapper;
import com.relyon.metasmart.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private com.relyon.metasmart.repository.PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthMapper authMapper;

    @Mock
    private EmailService emailService;

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private AuthService authService;

    private User user;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("encodedPassword")
                .build();

        registerRequest = RegisterRequest.builder()
                .name("John Doe")
                .email("john@example.com")
                .password("password123")
                .build();

        loginRequest = LoginRequest.builder()
                .email("john@example.com")
                .password("password123")
                .build();
    }

    @Nested
    @DisplayName("Register tests")
    class RegisterTests {

        @Test
        @DisplayName("Should register user successfully")
        void shouldRegisterUserSuccessfully() {
            when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
            when(authMapper.toEntity(registerRequest)).thenReturn(user);
            when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(jwtService.generateToken(user)).thenReturn("jwt-token");
            when(subscriptionService.isPremium(user)).thenReturn(false);

            var response = authService.register(registerRequest);

            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getEmail()).isEqualTo(user.getEmail());
            assertThat(response.getName()).isEqualTo(user.getName());
            assertThat(response.isPremium()).isFalse();

            verify(userRepository).existsByEmail(registerRequest.getEmail());
            verify(userRepository).save(any(User.class));
            verify(jwtService).generateToken(user);
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailExists() {
            when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessage(ErrorMessages.EMAIL_ALREADY_EXISTS);

            verify(userRepository).existsByEmail(registerRequest.getEmail());
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Login tests")
    class LoginTests {

        @Test
        @DisplayName("Should login user successfully")
        void shouldLoginUserSuccessfully() {
            when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);
            when(jwtService.generateToken(user)).thenReturn("jwt-token");
            when(subscriptionService.isPremium(user)).thenReturn(true);

            var response = authService.login(loginRequest);

            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getEmail()).isEqualTo(user.getEmail());
            assertThat(response.getName()).isEqualTo(user.getName());
            assertThat(response.isPremium()).isTrue();

            verify(userRepository).findByEmail(loginRequest.getEmail());
            verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPassword());
            verify(jwtService).generateToken(user);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage(ErrorMessages.INVALID_CREDENTIALS);

            verify(userRepository).findByEmail(loginRequest.getEmail());
            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("Should throw exception when password is invalid")
        void shouldThrowExceptionWhenPasswordInvalid() {
            when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(false);

            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage(ErrorMessages.INVALID_CREDENTIALS);

            verify(userRepository).findByEmail(loginRequest.getEmail());
            verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPassword());
            verify(jwtService, never()).generateToken(any());
        }
    }

    @Nested
    @DisplayName("Forgot password tests")
    class ForgotPasswordTests {

        @Test
        @DisplayName("Should process forgot password for existing user")
        void shouldProcessForgotPasswordForExistingUser() {
            var request = ForgotPasswordRequest.builder()
                    .email("john@example.com")
                    .build();

            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

            authService.forgotPassword(request);

            verify(passwordResetTokenRepository).invalidateAllTokensForUser(user);
            verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
            verify(emailService).sendPasswordResetEmail(eq(user.getEmail()), anyString(), eq(user.getName()));
        }

        @Test
        @DisplayName("Should silently return for non-existent email")
        void shouldSilentlyReturnForNonExistentEmail() {
            var request = ForgotPasswordRequest.builder()
                    .email("nonexistent@example.com")
                    .build();

            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

            authService.forgotPassword(request);

            verify(passwordResetTokenRepository, never()).save(any());
            verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Reset password tests")
    class ResetPasswordTests {

        @Test
        @DisplayName("Should reset password successfully with valid token")
        void shouldResetPasswordSuccessfully() {
            var request = ResetPasswordRequest.builder()
                    .token("valid-token")
                    .newPassword("NewPassword123!")
                    .build();

            var resetToken = PasswordResetToken.builder()
                    .id(1L)
                    .token("valid-token")
                    .user(user)
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .used(false)
                    .build();

            when(passwordResetTokenRepository.findByTokenAndUsedFalse(request.getToken()))
                    .thenReturn(Optional.of(resetToken));
            when(passwordEncoder.encode(request.getNewPassword())).thenReturn("encodedNewPassword");

            authService.resetPassword(request);

            verify(userRepository).save(user);
            verify(passwordResetTokenRepository).save(resetToken);
            assertThat(resetToken.getUsed()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception for invalid token")
        void shouldThrowExceptionForInvalidToken() {
            var request = ResetPasswordRequest.builder()
                    .token("invalid-token")
                    .newPassword("NewPassword123!")
                    .build();

            when(passwordResetTokenRepository.findByTokenAndUsedFalse(request.getToken()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.resetPassword(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Invalid or expired password reset token");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception for expired token")
        void shouldThrowExceptionForExpiredToken() {
            var request = ResetPasswordRequest.builder()
                    .token("expired-token")
                    .newPassword("NewPassword123!")
                    .build();

            var expiredToken = PasswordResetToken.builder()
                    .id(1L)
                    .token("expired-token")
                    .user(user)
                    .expiresAt(LocalDateTime.now().minusHours(1))
                    .used(false)
                    .build();

            when(passwordResetTokenRepository.findByTokenAndUsedFalse(request.getToken()))
                    .thenReturn(Optional.of(expiredToken));

            assertThatThrownBy(() -> authService.resetPassword(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Password reset token has expired");

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Validate reset token tests")
    class ValidateResetTokenTests {

        @Test
        @DisplayName("Should return true for valid token")
        void shouldReturnTrueForValidToken() {
            var validToken = PasswordResetToken.builder()
                    .id(1L)
                    .token("valid-token")
                    .user(user)
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .used(false)
                    .build();

            when(passwordResetTokenRepository.findByTokenAndUsedFalse("valid-token"))
                    .thenReturn(Optional.of(validToken));

            var result = authService.validateResetToken("valid-token");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false for expired token")
        void shouldReturnFalseForExpiredToken() {
            var expiredToken = PasswordResetToken.builder()
                    .id(1L)
                    .token("expired-token")
                    .user(user)
                    .expiresAt(LocalDateTime.now().minusHours(1))
                    .used(false)
                    .build();

            when(passwordResetTokenRepository.findByTokenAndUsedFalse("expired-token"))
                    .thenReturn(Optional.of(expiredToken));

            var result = authService.validateResetToken("expired-token");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for non-existent token")
        void shouldReturnFalseForNonExistentToken() {
            when(passwordResetTokenRepository.findByTokenAndUsedFalse("non-existent"))
                    .thenReturn(Optional.empty());

            var result = authService.validateResetToken("non-existent");

            assertThat(result).isFalse();
        }
    }
}
