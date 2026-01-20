package com.relyon.metasmart.entity.actionplan.dto;

import com.relyon.metasmart.entity.actionplan.RescheduleReason;
import com.relyon.metasmart.entity.actionplan.ScheduleSlotCreationType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleSlotRequest {

    @NotNull(message = "Slot index is required")
    @Min(value = 0, message = "Slot index must be at least 0")
    @Max(value = 23, message = "Slot index must be at most 23")
    private Integer slotIndex;

    @Size(max = 5, message = "Specific time must be in HH:mm format")
    private String specificTime;

    @Builder.Default
    private ScheduleSlotCreationType createdVia = ScheduleSlotCreationType.MANUAL;

    private RescheduleReason rescheduleReason;
}
