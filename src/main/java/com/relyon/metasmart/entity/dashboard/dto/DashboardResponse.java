package com.relyon.metasmart.entity.dashboard.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private long activeGoalsCount;
    private long completedGoalsCount;
    private long pendingReflectionsCount;
    private long unreadNudgesCount;
    private int streakShieldsAvailable;
    private List<StreakAtRiskDto> streaksAtRisk;
}
