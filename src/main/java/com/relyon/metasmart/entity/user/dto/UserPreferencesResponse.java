package com.relyon.metasmart.entity.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferencesResponse {

    private Long id;
    private String timezone;
    private String language;
    private Boolean emailNotifications;
    private Boolean pushNotifications;
    private Boolean weeklyDigest;
    private Boolean streakReminders;
    private Boolean guardianNudges;
    private String preferredReminderTime;
}
