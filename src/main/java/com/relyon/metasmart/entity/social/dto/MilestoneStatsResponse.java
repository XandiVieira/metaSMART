package com.relyon.metasmart.entity.social.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MilestoneStatsResponse {
    Long goalId;
    int milestonePercentage;
    long usersReachedThisMilestone;
    double percentageOfUsersAtThisPoint;
    int averageDaysToReach;
    String motivationalMessage;
}
