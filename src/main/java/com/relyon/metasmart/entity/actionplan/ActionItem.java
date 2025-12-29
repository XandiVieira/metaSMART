package com.relyon.metasmart.entity.actionplan;

import com.relyon.metasmart.entity.AuditableEntity;
import com.relyon.metasmart.entity.goal.Goal;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
