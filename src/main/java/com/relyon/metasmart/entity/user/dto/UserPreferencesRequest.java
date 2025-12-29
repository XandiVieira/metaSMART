package com.relyon.metasmart.entity.user.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferencesRequest {

    private String timezone;

    private String language;

    private Boolean emailNotifications;

    private Boolean pushNotifications;

    private Boolean weeklyDigest;

    private Boolean streakReminders;

    private Boolean guardianNudges;

    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Time must be in HH:mm format")
    private String preferredReminderTime;
}
