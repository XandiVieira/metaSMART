package com.relyon.metasmart.entity.actionplan;

import com.relyon.metasmart.entity.AuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "task_schedule_slots")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TaskScheduleSlot extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_item_id", nullable = false)
    private ActionItem actionItem;

    @Column(nullable = false)
    private Integer slotIndex;

    @Column(length = 5)
    private String specificTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ScheduleSlotCreationType createdVia = ScheduleSlotCreationType.MANUAL;

    @Column(nullable = false)
    private LocalDate effectiveFrom;

    @Column
    private LocalDate effectiveUntil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rescheduled_from_slot_id")
    private TaskScheduleSlot rescheduledFromSlot;

    @Enumerated(EnumType.STRING)
    @Column
    private RescheduleReason rescheduleReason;

    public boolean isActive() {
        var today = LocalDate.now();
        return !today.isBefore(effectiveFrom)
            && (effectiveUntil == null || !today.isAfter(effectiveUntil));
    }

    public boolean isActive(LocalDate date) {
        return !date.isBefore(effectiveFrom)
            && (effectiveUntil == null || !date.isAfter(effectiveUntil));
    }
}
