package com.relyon.metasmart.controller;

import com.relyon.metasmart.constant.ApiPaths;
import com.relyon.metasmart.entity.user.dto.AuthResponse;
import com.relyon.metasmart.entity.user.dto.ForgotPasswordRequest;
import com.relyon.metasmart.entity.user.dto.LoginRequest;
import com.relyon.metasmart.entity.user.dto.RegisterRequest;
import com.relyon.metasmart.entity.user.dto.ResetPasswordRequest;
import com.relyon.metasmart.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping(ApiPaths.AUTH)
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping(ApiPaths.REGISTER)
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping(ApiPaths.LOGIN)
    @Operation(summary = "Login with email and password")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset email")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.debug("Forgot password request for email: {}", request.getEmail());
        authService.forgotPassword(request);
        return ResponseEntity.ok(Map.of(
                "message", "If an account exists with this email, a password reset link has been sent"
        ));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using token")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.debug("Reset password request received");
        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of(
                "message", "Password has been reset successfully"
        ));
    }

    @GetMapping("/validate-reset-token")
    @Operation(summary = "Validate a password reset token")
    public ResponseEntity<Map<String, Boolean>> validateResetToken(@RequestParam String token) {
        log.debug("Validating reset token");
        var isValid = authService.validateResetToken(token);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }
}
