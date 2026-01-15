package com.relyon.metasmart.entity.dashboard.dto;

import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalStatsResponse {

    private long totalGoals;
    private long activeGoals;
    private long completedGoals;
    private long pausedGoals;
    private long abandonedGoals;
    private BigDecimal completionRate;
    private int bestStreak;
    private int currentBestStreak;
    private Map<String, Long> goalsByCategory;
}
