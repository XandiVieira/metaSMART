package com.relyon.metasmart.entity.actionplan.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledTaskRequest {

    @NotNull(message = "Task ID is required")
    private Long taskId;

    @NotNull(message = "Scheduled date is required")
    private LocalDate scheduledDate;
}
