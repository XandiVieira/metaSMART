package com.relyon.metasmart.entity.goal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmartPillarsDto {

    private boolean specific;
    private boolean measurable;
    private boolean achievable;
    private boolean relevant;
    private boolean timeBound;
    private int completionPercentage;
}
