package com.relyon.metasmart.entity.reflection.dto;

import com.relyon.metasmart.entity.reflection.ReflectionFrequency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingReflectionResponse {

    private Long goalId;
    private String goalTitle;
    private String goalCategory;
    private ReflectionFrequency frequency;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Integer daysOverdue;
}
