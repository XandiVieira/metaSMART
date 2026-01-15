package com.relyon.metasmart.config.subscription;

import com.relyon.metasmart.entity.subscription.SubscriptionTier;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.SubscriptionRequiredException;
import com.relyon.metasmart.service.SubscriptionService;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class SubscriptionEnforcementAspect {

    private final SubscriptionService subscriptionService;

    @Before("@annotation(requiresSubscription)")
    public void checkSubscription(JoinPoint joinPoint, RequiresSubscription requiresSubscription) {
        var user = getCurrentUser();
        if (user == null) {
            log.warn("No authenticated user found for subscription check");
            throw new SubscriptionRequiredException("Authentication required");
        }

        var entitlements = subscriptionService.getEntitlements(user);
        var requiredTier = requiresSubscription.minTier();
        var requiredFeatures = requiresSubscription.features();

        if (requiredTier == SubscriptionTier.PREMIUM && !entitlements.getIsPremium()) {
            log.debug("User {} denied access to premium feature at {}", user.getId(), joinPoint.getSignature().getName());
            throw new SubscriptionRequiredException(joinPoint.getSignature().getName(), "Premium");
        }

        if (requiredFeatures.length > 0) {
            var features = entitlements.getFeatures();
            var missingFeature = Arrays.stream(requiredFeatures)
                    .filter(feature -> !Boolean.TRUE.equals(features.get(feature)))
                    .findFirst();

            if (missingFeature.isPresent()) {
                log.debug("User {} denied access to feature {} at {}", user.getId(), missingFeature.get(), joinPoint.getSignature().getName());
                throw new SubscriptionRequiredException(missingFeature.get(), "Premium");
            }
        }

        log.trace("Subscription check passed for user {} at {}", user.getId(), joinPoint.getSignature().getName());
    }

    private User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user;
        }
        return null;
    }
}
