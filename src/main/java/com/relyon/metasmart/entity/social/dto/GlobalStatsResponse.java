package com.relyon.metasmart.entity.social.dto;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class GlobalStatsResponse {
    long totalActiveUsers;
    long totalGoalsCreated;
    long totalGoalsCompleted;
    double overallCompletionRate;
    long totalProgressEntries;
    Map<String, Long> goalsByCategory;
    Map<String, Double> completionRateByCategory;
    int averageStreakAcrossUsers;
    int longestStreakEver;
}
