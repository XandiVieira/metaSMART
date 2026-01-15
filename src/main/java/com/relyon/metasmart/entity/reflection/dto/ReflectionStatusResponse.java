package com.relyon.metasmart.entity.reflection.dto;

import com.relyon.metasmart.entity.reflection.ReflectionFrequency;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReflectionStatusResponse {

    private Long goalId;
    private String goalTitle;
    private ReflectionFrequency frequency;
    private Integer frequencyDays;
    private LocalDate currentPeriodStart;
    private LocalDate currentPeriodEnd;
    private Boolean reflectionDue;
    private Boolean reflectionCompleted;
    private LocalDate lastReflectionDate;
    private Integer totalReflections;
    private Double averageRating;
}
