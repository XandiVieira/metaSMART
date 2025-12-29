package com.relyon.metasmart.entity.goal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalPillarsDto {

    private String clarity;
    private String metric;
    private String actionPlan;
    private String deadline;
    private String motivation;
}
