package com.relyon.metasmart.entity.actionplan.dto;

import com.relyon.metasmart.entity.actionplan.RecurrenceFrequency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRecurrenceDto {

    private Boolean enabled;
    private RecurrenceFrequency frequency;
    private Integer interval;
    private List<Integer> daysOfWeek;
    private LocalDate endsAt;
}
