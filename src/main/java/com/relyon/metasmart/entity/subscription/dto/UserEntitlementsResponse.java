package com.relyon.metasmart.entity.subscription.dto;

import com.relyon.metasmart.entity.subscription.SubscriptionTier;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntitlementsResponse {

    private SubscriptionTier tier;
    private Boolean isPremium;

    private Integer maxActiveGoals;
    private Integer maxGuardiansPerGoal;
    private Integer progressHistoryDays;
    private Integer streakShieldsPerMonth;
    private Integer strugglingRequestsPerMonth;

    private Integer streakShieldsAvailable;
    private Integer strugglingAssistsAvailable;
    private Integer goalBoostsAvailable;
    private Integer guardianSlotsAvailable;

    private Map<String, Boolean> features;
}
