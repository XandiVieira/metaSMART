package com.relyon.metasmart.entity.notification;

import com.relyon.metasmart.entity.AuditableEntity;
import com.relyon.metasmart.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "notification_preferences")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferences extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Push Notification Settings
    @Builder.Default
    @Column(nullable = false)
    private Boolean pushEnabled = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean pushGoalReminders = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean pushProgressReminders = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean pushMilestones = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean pushStreakAlerts = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean pushGuardianNudges = true;

    // Email Settings
    @Builder.Default
    @Column(nullable = false)
    private Boolean emailEnabled = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean emailWeeklyDigest = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean emailMilestones = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean emailStreakAtRisk = true;

    // WhatsApp Settings (future)
    @Builder.Default
    @Column(nullable = false)
    private Boolean whatsappEnabled = false;

    private String whatsappNumber;

    // Quiet Hours
    @Builder.Default
    @Column(nullable = false)
    private Boolean quietHoursEnabled = false;

    private String quietHoursStart; // HH:mm

    private String quietHoursEnd; // HH:mm
}
