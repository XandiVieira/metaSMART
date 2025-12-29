package com.relyon.metasmart.entity.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferencesResponse {

    private Long id;

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
    private String whatsappNumber;

    // Quiet Hours
    private Boolean quietHoursEnabled;
    private String quietHoursStart;
    private String quietHoursEnd;
}
