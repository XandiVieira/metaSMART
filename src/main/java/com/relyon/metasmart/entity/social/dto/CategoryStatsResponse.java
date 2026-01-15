package com.relyon.metasmart.entity.social.dto;

import com.relyon.metasmart.entity.goal.GoalCategory;
import com.relyon.metasmart.entity.struggling.StrugglingType;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CategoryStatsResponse {
    GoalCategory category;
    long totalUsers;
    long activeGoals;
    long completedGoals;
    double averageCompletionRate;
    double averageDaysToComplete;
    int averageStreak;
    int longestStreak;
    List<StrugglingType> commonObstacles;
    List<String> topStrategies;
}
