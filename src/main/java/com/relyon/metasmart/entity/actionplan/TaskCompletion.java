package com.relyon.metasmart.entity.actionplan;

import com.relyon.metasmart.entity.AuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "task_completions")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCompletion extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_item_id", nullable = false)
    private ActionItem actionItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_slot_id")
    private TaskScheduleSlot scheduleSlot;

    @Column(nullable = false)
    private LocalDate periodStart;

    @Column(nullable = false)
    private LocalDate scheduledDate;

    @Column(length = 5)
    private String scheduledTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CompletionStatus status = CompletionStatus.PENDING;

    @Column
    private LocalDateTime completedAt;

    @Column(length = 500)
    private String note;

    @Deprecated
    @Transient
    public LocalDate getDate() {
        return scheduledDate;
    }

    @Deprecated
    @Transient
    public void setDate(LocalDate date) {
        this.scheduledDate = date;
    }
}
