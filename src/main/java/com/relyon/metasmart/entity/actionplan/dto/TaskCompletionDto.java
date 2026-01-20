package com.relyon.metasmart.entity.actionplan.dto;

import com.relyon.metasmart.entity.actionplan.CompletionStatus;
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
public class TaskCompletionDto {

    private Long id;
    private Long scheduleSlotId;
    private LocalDate periodStart;
    private LocalDate scheduledDate;
    private String scheduledTime;
    private CompletionStatus status;
    private LocalDateTime completedAt;
    private String note;
}
