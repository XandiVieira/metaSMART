package com.relyon.metasmart.service;

import com.relyon.metasmart.entity.subscription.*;
import com.relyon.metasmart.entity.subscription.dto.PurchaseResponse;
import com.relyon.metasmart.entity.subscription.dto.SubscriptionResponse;
import com.relyon.metasmart.entity.subscription.dto.UserEntitlementsResponse;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.repository.UserPurchaseRepository;
import com.relyon.metasmart.repository.UserSubscriptionRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private static final int FREE_MAX_GOALS = 3;
    private static final int PREMIUM_MAX_GOALS = Integer.MAX_VALUE;

    private static final int FREE_MAX_GUARDIANS = 1;
    private static final int PREMIUM_MAX_GUARDIANS = 5;

    private static final int FREE_PROGRESS_HISTORY_DAYS = 30;
    private static final int PREMIUM_PROGRESS_HISTORY_DAYS = Integer.MAX_VALUE;

    private static final int FREE_STREAK_SHIELDS = 1;
    private static final int PREMIUM_STREAK_SHIELDS = 3;

    private static final int FREE_STRUGGLING_REQUESTS = 1;
    private static final int PREMIUM_STRUGGLING_REQUESTS = Integer.MAX_VALUE;

    private final UserSubscriptionRepository subscriptionRepository;
    private final UserPurchaseRepository purchaseRepository;

    public SubscriptionResponse getCurrentSubscription(User user) {
        log.debug("Getting current subscription for user ID: {}", user.getId());

        return subscriptionRepository.findActiveSubscription(user)
                .map(this::toSubscriptionResponse)
                .orElseGet(this::buildFreeSubscriptionResponse);
    }

    public UserEntitlementsResponse getEntitlements(User user) {
        log.debug("Getting entitlements for user ID: {}", user.getId());

        var subscription = subscriptionRepository.findActiveSubscription(user).orElse(null);
        var isPremium = subscription != null && subscription.isPremium();
        var tier = isPremium ? SubscriptionTier.PREMIUM : SubscriptionTier.FREE;
        var now = LocalDateTime.now();

        return UserEntitlementsResponse.builder()
                .tier(tier)
                .isPremium(isPremium)
                .maxActiveGoals(isPremium ? PREMIUM_MAX_GOALS : FREE_MAX_GOALS)
                .maxGuardiansPerGoal(isPremium ? PREMIUM_MAX_GUARDIANS : FREE_MAX_GUARDIANS)
                .progressHistoryDays(isPremium ? PREMIUM_PROGRESS_HISTORY_DAYS : FREE_PROGRESS_HISTORY_DAYS)
                .streakShieldsPerMonth(isPremium ? PREMIUM_STREAK_SHIELDS : FREE_STREAK_SHIELDS)
                .strugglingRequestsPerMonth(isPremium ? PREMIUM_STRUGGLING_REQUESTS : FREE_STRUGGLING_REQUESTS)
                .streakShieldsAvailable(purchaseRepository.countAvailable(user, PurchaseType.STREAK_SHIELD, now))
                .strugglingAssistsAvailable(purchaseRepository.countAvailable(user, PurchaseType.STRUGGLING_ASSIST, now))
                .goalBoostsAvailable(purchaseRepository.countAvailable(user, PurchaseType.GOAL_BOOST, now))
                .guardianSlotsAvailable(purchaseRepository.countAvailable(user, PurchaseType.GUARDIAN_SLOT, now))
                .features(buildFeatureMap(isPremium))
                .build();
    }

    public List<PurchaseResponse> getPurchases(User user) {
        log.debug("Getting purchases for user ID: {}", user.getId());

        return purchaseRepository.findByUserOrderByPurchasedAtDesc(user).stream()
                .map(this::toPurchaseResponse)
                .toList();
    }

    public boolean isPremium(User user) {
        return subscriptionRepository.findActiveSubscription(user)
                .map(UserSubscription::isPremium)
                .orElse(false);
    }

    public int getAvailablePurchaseCount(User user, PurchaseType type) {
        return purchaseRepository.countAvailable(user, type, LocalDateTime.now());
    }

    @Transactional
    public boolean consumePurchase(User user, PurchaseType type) {
        var now = LocalDateTime.now();
        var purchases = purchaseRepository.findAvailablePurchases(user, type, now);

        if (purchases.isEmpty()) {
            return false;
        }

        var purchase = purchases.getFirst();
        purchase.useOne();
        purchaseRepository.save(purchase);

        log.info("Consumed one {} for user ID: {}. Remaining: {}", type, user.getId(), purchase.getQuantityRemaining());
        return true;
    }

    private Map<String, Boolean> buildFeatureMap(boolean isPremium) {
        var features = new HashMap<String, Boolean>();
        features.put("unlimitedGoals", isPremium);
        features.put("multipleGuardians", isPremium);
        features.put("unlimitedHistory", isPremium);
        features.put("createTemplates", isPremium);
        features.put("aiInsights", isPremium);
        features.put("dataExport", isPremium);
        features.put("prioritySupport", isPremium);
        features.put("weeklyReflections", true);
        features.put("basicAchievements", true);
        features.put("guardianSystem", true);
        return features;
    }

    private SubscriptionResponse toSubscriptionResponse(UserSubscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .tier(subscription.getTier())
                .status(subscription.getStatus())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .trialEndDate(subscription.getTrialEndDate())
                .billingPeriod(subscription.getBillingPeriod())
                .isActive(subscription.isActive())
                .isPremium(subscription.isPremium())
                .build();
    }

    private SubscriptionResponse buildFreeSubscriptionResponse() {
        return SubscriptionResponse.builder()
                .tier(SubscriptionTier.FREE)
                .status(SubscriptionStatus.ACTIVE)
                .isActive(true)
                .isPremium(false)
                .build();
    }

    private PurchaseResponse toPurchaseResponse(UserPurchase purchase) {
        return PurchaseResponse.builder()
                .id(purchase.getId())
                .purchaseType(purchase.getPurchaseType())
                .quantity(purchase.getQuantity())
                .quantityRemaining(purchase.getQuantityRemaining())
                .priceAmount(purchase.getPriceAmount())
                .priceCurrency(purchase.getPriceCurrency())
                .purchasedAt(purchase.getPurchasedAt())
                .expiresAt(purchase.getExpiresAt())
                .build();
    }
}
