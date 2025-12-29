package com.relyon.metasmart.entity.actionplan.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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
