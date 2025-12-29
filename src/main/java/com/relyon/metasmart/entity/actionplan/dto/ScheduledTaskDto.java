package com.relyon.metasmart.entity.actionplan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledTaskDto {

    private Long id;
    private Long taskId;
    private LocalDate scheduledDate;
    private Boolean completed;
    private LocalDateTime completedAt;
}
