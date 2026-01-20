package com.relyon.metasmart.entity.actionplan.dto;

import com.relyon.metasmart.entity.actionplan.RescheduleReason;
import com.relyon.metasmart.entity.actionplan.ScheduleSlotCreationType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleSlotResponse {

    private Long id;
    private Long actionItemId;
    private Integer slotIndex;
    private String specificTime;
    private ScheduleSlotCreationType createdVia;
    private LocalDate effectiveFrom;
    private LocalDate effectiveUntil;
    private Long rescheduledFromSlotId;
    private RescheduleReason rescheduleReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean active;
}
