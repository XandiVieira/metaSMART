package com.relyon.metasmart.entity.notification.dto;

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
public class NotificationPreferencesRequest {

    // Push
    private Boolean pushEnabled;
    private Boolean pushGoalReminders;
    private Boolean pushProgressReminders;
    private Boolean pushMilestones;
    private Boolean pushStreakAlerts;
    private Boolean pushGuardianNudges;

    // Email
    private Boolean emailEnabled;
    private Boolean emailWeeklyDigest;
    private Boolean emailMilestones;
    private Boolean emailStreakAtRisk;

    // WhatsApp
    private Boolean whatsappEnabled;

    @Size(max = 20, message = "Phone number must be at most 20 characters")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String whatsappNumber;

    // Quiet Hours
    private Boolean quietHoursEnabled;

    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Time must be in HH:mm format")
    private String quietHoursStart;

    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Time must be in HH:mm format")
    private String quietHoursEnd;
}
