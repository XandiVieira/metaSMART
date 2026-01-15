package com.relyon.metasmart.controller;

import com.relyon.metasmart.constant.ApiPaths;
import com.relyon.metasmart.entity.notification.dto.NotificationPreferencesRequest;
import com.relyon.metasmart.entity.notification.dto.NotificationPreferencesResponse;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.entity.user.dto.UpdateProfileRequest;
import com.relyon.metasmart.entity.user.dto.UserPreferencesRequest;
import com.relyon.metasmart.entity.user.dto.UserPreferencesResponse;
import com.relyon.metasmart.entity.user.dto.UserProfileResponse;
import com.relyon.metasmart.service.NotificationPreferencesService;
import com.relyon.metasmart.service.UserPreferencesService;
import com.relyon.metasmart.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(ApiPaths.USERS)
@RequiredArgsConstructor
@Tag(name = "User Profile")
public class UserController {

    private final UserProfileService userProfileService;
    private final UserPreferencesService userPreferencesService;
    private final NotificationPreferencesService notificationPreferencesService;

    @GetMapping("/profile")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal User user) {
        log.debug("Getting profile for user: {}", user.getEmail());
        return ResponseEntity.ok(userProfileService.getProfile(user));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateProfileRequest request) {
        log.debug("Updating profile for user: {}", user.getEmail());
        return ResponseEntity.ok(userProfileService.updateProfile(user, request));
    }

    @PostMapping("/streak-shields/use")
    @Operation(summary = "Use a streak shield to protect a streak")
    public ResponseEntity<Map<String, Object>> useStreakShield(@AuthenticationPrincipal User user) {
        log.debug("Using streak shield for user: {}", user.getEmail());
        var success = userProfileService.useStreakShield(user);
        if (success) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Streak shield used successfully",
                    "remainingShields", user.getStreakShields()
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "No streak shields available"
            ));
        }
    }

    @GetMapping("/preferences")
    @Operation(summary = "Get user preferences")
    public ResponseEntity<UserPreferencesResponse> getPreferences(@AuthenticationPrincipal User user) {
        log.debug("Getting preferences for user: {}", user.getEmail());
        return ResponseEntity.ok(userPreferencesService.getPreferences(user));
    }

    @PutMapping("/preferences")
    @Operation(summary = "Update user preferences")
    public ResponseEntity<UserPreferencesResponse> updatePreferences(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UserPreferencesRequest request) {
        log.debug("Updating preferences for user: {}", user.getEmail());
        return ResponseEntity.ok(userPreferencesService.updatePreferences(user, request));
    }

    @GetMapping("/notifications/preferences")
    @Operation(summary = "Get notification preferences")
    public ResponseEntity<NotificationPreferencesResponse> getNotificationPreferences(@AuthenticationPrincipal User user) {
        log.debug("Getting notification preferences for user: {}", user.getEmail());
        return ResponseEntity.ok(notificationPreferencesService.getPreferences(user));
    }

    @PutMapping("/notifications/preferences")
    @Operation(summary = "Update notification preferences")
    public ResponseEntity<NotificationPreferencesResponse> updateNotificationPreferences(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody NotificationPreferencesRequest request) {
        log.debug("Updating notification preferences for user: {}", user.getEmail());
        return ResponseEntity.ok(notificationPreferencesService.updatePreferences(user, request));
    }
}
