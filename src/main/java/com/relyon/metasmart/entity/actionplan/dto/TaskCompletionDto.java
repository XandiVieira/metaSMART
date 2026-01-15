package com.relyon.metasmart.entity.actionplan.dto;

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
    private LocalDate date;
    private LocalDateTime completedAt;
    private String note;
}
