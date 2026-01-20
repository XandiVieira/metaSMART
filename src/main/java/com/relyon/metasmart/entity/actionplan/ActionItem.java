package com.relyon.metasmart.entity.actionplan;

import com.relyon.metasmart.entity.AuditableEntity;
import com.relyon.metasmart.entity.goal.Goal;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "action_items")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ActionItem extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TaskType taskType = TaskType.ONE_TIME;

    @Column
    private LocalDate targetDate;

    @Builder.Default
    @Column(nullable = false)
    private Boolean completed = false;

    @Column
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column
    @Builder.Default
    private Integer impactScore = 5;

    @Column
    @Builder.Default
    private Integer effortEstimate = 5;

    @Column(length = 500)
    private String context;

    @Column(length = 500)
    private String dependencies;

    @Embedded
    private TaskRecurrence recurrence;

    @Embedded
    private FrequencyGoal frequencyGoal;

    @Embedded
    private ReminderOverride reminderOverride;

    @Column(name = "target_per_completion", precision = 19, scale = 2)
    private BigDecimal targetPerCompletion;

    @Column(name = "target_unit", length = 50)
    private String targetUnit;

    @Builder.Default
    @Column(name = "notify_on_scheduled_time", nullable = false)
    private Boolean notifyOnScheduledTime = false;

    @Column(name = "notify_minutes_before")
    private Integer notifyMinutesBefore;

    @Column(length = 1000)
    private String notes;

    @Deprecated
    @Transient
    public LocalDate getDueDate() {
        return targetDate;
    }

    @Deprecated
    @Transient
    public void setDueDate(LocalDate dueDate) {
        this.targetDate = dueDate;
    }
}
