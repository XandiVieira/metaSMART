package com.relyon.metasmart.entity.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferencesRequest {

    @Size(max = 100, message = "Timezone must be at most 100 characters")
    private String timezone;

    @Size(min = 2, max = 10, message = "Language code must be between 2 and 10 characters")
    private String language;

    private Boolean emailNotifications;

    private Boolean pushNotifications;

    private Boolean weeklyDigest;

    private Boolean streakReminders;

    private Boolean guardianNudges;

    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Time must be in HH:mm format")
    private String preferredReminderTime;
}
