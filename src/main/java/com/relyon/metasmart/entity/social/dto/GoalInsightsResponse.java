package com.relyon.metasmart.entity.social.dto;

import com.relyon.metasmart.entity.goal.GoalCategory;
import com.relyon.metasmart.entity.struggling.StrugglingType;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class GoalInsightsResponse {
    Long goalId;
    GoalCategory category;
    long usersWithSimilarGoals;
    double similarGoalsCompletionRate;
    int averageDaysToComplete;
    List<StrugglingType> commonObstacles;
    List<String> suggestedStrategies;
    String encouragementMessage;
}
