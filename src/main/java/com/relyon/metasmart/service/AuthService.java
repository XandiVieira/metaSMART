package com.relyon.metasmart.service;

import com.relyon.metasmart.config.JwtService;
import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.entity.user.dto.AuthResponse;
import com.relyon.metasmart.entity.user.dto.LoginRequest;
import com.relyon.metasmart.entity.user.dto.RegisterRequest;
import com.relyon.metasmart.exception.AuthenticationException;
import com.relyon.metasmart.exception.DuplicateResourceException;
import com.relyon.metasmart.mapper.AuthMapper;
import com.relyon.metasmart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthMapper authMapper;

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
        return buildAuthResponse(user.getEmail(), user.getName(), token);
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
        return buildAuthResponse(user.getEmail(), user.getName(), token);
    }

    private AuthResponse buildAuthResponse(String email, String name, String token) {
        return AuthResponse.builder()
                .token(token)
                .email(email)
                .name(name)
                .build();
    }
}
