package com.relyon.metasmart.entity.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreakAtRiskDto {

    private Long goalId;
    private String goalTitle;
    private int currentStreak;
    private int daysWithoutProgress;
}
