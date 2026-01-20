package com.relyon.metasmart.entity.user;

import com.relyon.metasmart.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "user_preferences")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferences extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder.Default
    @Column(nullable = false)
    private String timezone = "UTC";

    @Builder.Default
    @Column(name = "week_start_day", nullable = false)
    private Integer weekStartDay = 1;

    @Builder.Default
    @Column(nullable = false)
    private String language = "en";

    @Builder.Default
    @Column(nullable = false)
    private Boolean emailNotifications = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean pushNotifications = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean weeklyDigest = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean streakReminders = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean guardianNudges = true;

    @Column
    private String preferredReminderTime; // HH:mm format
}
