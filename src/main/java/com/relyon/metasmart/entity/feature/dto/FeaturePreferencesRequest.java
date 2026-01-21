package com.relyon.metasmart.entity.feature.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeaturePreferencesRequest {

    private Boolean dailyJournalEnabled;

    private Boolean streaksEnabled;

    private Boolean achievementsEnabled;

    private Boolean analyticsEnabled;

    private Boolean flightPlanEnabled;

    private Boolean progressRemindersEnabled;

    private Boolean milestonesEnabled;

    private Boolean obstacleTrackingEnabled;

    private Boolean reflectionsEnabled;

    private Boolean socialProofEnabled;
}
