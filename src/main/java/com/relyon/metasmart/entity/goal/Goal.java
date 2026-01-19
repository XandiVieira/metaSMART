package com.relyon.metasmart.entity.goal;

import com.relyon.metasmart.entity.AuditableEntity;
import com.relyon.metasmart.entity.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "goals")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Goal extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalCategory goalCategory;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal targetValue;

    @Column(nullable = false)
    private String unit;

    @Builder.Default
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal currentProgress = BigDecimal.ZERO;

    @Column(length = 500)
    private String motivation;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate targetDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private GoalStatus goalStatus = GoalStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    private LocalDate archivedAt;

    private LocalDate deletedAt;

    @Builder.Default
    @Column(name = "created_during_premium")
    private boolean createdDuringPremium = false;

    @Column(name = "previous_status")
    @Enumerated(EnumType.STRING)
    private GoalStatus previousStatus;

    private LocalDate lastStreakShieldUsedAt;

    @Embedded
    private GoalPillars pillars;

    @Embedded
    private GoalMeasurement measurement;

    @Embedded
    private GoalReminders reminders;

    @Embedded
    private EmotionalAnchors emotionalAnchors;

    @Embedded
    private AiSupport aiSupport;

    @Column(length = 500)
    private String tags;

    @Column(name = "action_plan_overview", length = 2000)
    private String actionPlanOverview;

    @Builder.Default
    @Column(name = "streak")
    private Integer streak = 0;
}
