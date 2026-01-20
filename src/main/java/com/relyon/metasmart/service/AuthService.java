package com.relyon.metasmart.service;

import com.relyon.metasmart.config.JwtService;
import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.entity.user.PasswordResetToken;
import com.relyon.metasmart.entity.user.dto.*;
import com.relyon.metasmart.exception.AuthenticationException;
import com.relyon.metasmart.exception.BadRequestException;
import com.relyon.metasmart.exception.DuplicateResourceException;
import com.relyon.metasmart.mapper.AuthMapper;
import com.relyon.metasmart.repository.PasswordResetTokenRepository;
import com.relyon.metasmart.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int TOKEN_EXPIRY_HOURS = 24;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthMapper authMapper;
    private final EmailService emailService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.debug("Attempting to register user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed - email already exists: {}", request.getEmail());
            throw new DuplicateResourceException(ErrorMessages.EMAIL_ALREADY_EXISTS);
        }

        var user = authMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);
        log.info("User registered successfully with ID: {}", user.getId());

        var token = jwtService.generateToken(user);
        return buildAuthResponse(user.getEmail(), user.getName(), user.getProfilePictureUrl(), token);
    }

    public AuthResponse login(LoginRequest request) {
        log.debug("Attempting login for email: {}", request.getEmail());

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed - user not found for email: {}", request.getEmail());
                    return new AuthenticationException(ErrorMessages.INVALID_CREDENTIALS);
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed - invalid password for user ID: {}", user.getId());
            throw new AuthenticationException(ErrorMessages.INVALID_CREDENTIALS);
        }

        log.info("User logged in successfully with ID: {}", user.getId());
        var token = jwtService.generateToken(user);
        return buildAuthResponse(user.getEmail(), user.getName(), user.getProfilePictureUrl(), token);
    }

    private AuthResponse buildAuthResponse(String email, String name, String profilePictureUrl, String token) {
        return AuthResponse.builder()
                .token(token)
                .email(email)
                .name(name)
                .profilePictureUrl(profilePictureUrl)
                .build();
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        log.debug("Processing forgot password request for email: {}", request.getEmail());

        var userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            log.warn("Forgot password requested for non-existent email: {}", request.getEmail());
            // Don't reveal that email doesn't exist - just return silently
            return;
        }

        var user = userOptional.get();

        // Invalidate any existing tokens
        passwordResetTokenRepository.invalidateAllTokensForUser(user);

        // Generate new token
        var token = UUID.randomUUID().toString();
        var resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS))
                .used(false)
                .build();

        passwordResetTokenRepository.save(resetToken);
        log.info("Password reset token generated for user ID: {}", user.getId());

        // Send email
        emailService.sendPasswordResetEmail(user.getEmail(), token, user.getName());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.debug("Processing password reset with token");

        var resetToken = passwordResetTokenRepository.findByTokenAndUsedFalse(request.getToken())
                .orElseThrow(() -> {
                    log.warn("Invalid or expired password reset token");
                    return new BadRequestException("Invalid or expired password reset token");
                });

        if (!resetToken.isValid()) {
            log.warn("Password reset token is expired or already used");
            throw new BadRequestException("Password reset token has expired");
        }

        var user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        log.info("Password reset successfully for user ID: {}", user.getId());
    }

    @Transactional
    public boolean validateResetToken(String token) {
        return passwordResetTokenRepository.findByTokenAndUsedFalse(token)
                .map(PasswordResetToken::isValid)
                .orElse(false);
    }
}
