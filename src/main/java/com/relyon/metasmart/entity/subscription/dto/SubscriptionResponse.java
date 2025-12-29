package com.relyon.metasmart.entity.subscription.dto;

import com.relyon.metasmart.entity.subscription.SubscriptionStatus;
import com.relyon.metasmart.entity.subscription.SubscriptionTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {

    private Long id;
    private SubscriptionTier tier;
    private SubscriptionStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime trialEndDate;
    private String billingPeriod;
    private Boolean isActive;
    private Boolean isPremium;
}
