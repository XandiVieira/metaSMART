package com.relyon.metasmart.entity.actionplan.dto;

import com.relyon.metasmart.entity.actionplan.CompletionStatus;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCompletionRequest {

    private LocalDate scheduledDate;

    private Long scheduleSlotId;

    @Builder.Default
    private CompletionStatus status = CompletionStatus.COMPLETED;

    @Size(max = 5, message = "Scheduled time must be in HH:mm format")
    private String scheduledTime;

    @Size(max = 500, message = "Note must be at most 500 characters")
    private String note;
}
