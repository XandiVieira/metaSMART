package com.relyon.metasmart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.relyon.metasmart.entity.subscription.*;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.repository.UserPurchaseRepository;
import com.relyon.metasmart.repository.UserSubscriptionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private UserSubscriptionRepository subscriptionRepository;

    @Mock
    private UserPurchaseRepository purchaseRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private User user;
    private UserSubscription premiumSubscription;
    private UserPurchase purchase;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("John").email("john@test.com").build();
        premiumSubscription = UserSubscription.builder()
                .id(1L)
                .user(user)
                .tier(SubscriptionTier.PREMIUM)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDateTime.now().minusDays(30))
                .endDate(LocalDateTime.now().plusDays(30))
                .billingPeriod("MONTHLY")
                .build();
        purchase = UserPurchase.builder()
                .id(1L)
                .user(user)
                .purchaseType(PurchaseType.STREAK_SHIELD)
                .quantity(3)
                .quantityRemaining(2)
                .priceAmount(BigDecimal.valueOf(2.99))
                .priceCurrency("USD")
                .purchasedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
    }

    @Nested
    @DisplayName("Get current subscription tests")
    class GetCurrentSubscriptionTests {

        @Test
        @DisplayName("Should return premium subscription")
        void shouldReturnPremiumSubscription() {
            when(subscriptionRepository.findActiveSubscription(user)).thenReturn(Optional.of(premiumSubscription));

            var result = subscriptionService.getCurrentSubscription(user);

            assertThat(result.getTier()).isEqualTo(SubscriptionTier.PREMIUM);
            assertThat(result.getIsPremium()).isTrue();
            assertThat(result.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should return free subscription when no active subscription")
        void shouldReturnFreeSubscriptionWhenNoActiveSubscription() {
            when(subscriptionRepository.findActiveSubscription(user)).thenReturn(Optional.empty());

            var result = subscriptionService.getCurrentSubscription(user);

            assertThat(result.getTier()).isEqualTo(SubscriptionTier.FREE);
            assertThat(result.getIsPremium()).isFalse();
            assertThat(result.getIsActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("Get entitlements tests")
    class GetEntitlementsTests {

        @Test
        @DisplayName("Should return premium entitlements")
        void shouldReturnPremiumEntitlements() {
            when(subscriptionRepository.findActiveSubscription(user)).thenReturn(Optional.of(premiumSubscription));
            when(purchaseRepository.countAvailable(eq(user), any(), any())).thenReturn(0);

            var result = subscriptionService.getEntitlements(user);

            assertThat(result.getTier()).isEqualTo(SubscriptionTier.PREMIUM);
            assertThat(result.getIsPremium()).isTrue();
            assertThat(result.getMaxActiveGoals()).isEqualTo(Integer.MAX_VALUE);
            assertThat(result.getMaxGuardiansPerGoal()).isEqualTo(5);
            assertThat(result.getProgressHistoryDays()).isEqualTo(Integer.MAX_VALUE);
            assertThat(result.getStreakShieldsPerMonth()).isEqualTo(3);
            assertThat(result.getStrugglingRequestsPerMonth()).isEqualTo(Integer.MAX_VALUE);
            assertThat(result.getFeatures().get("unlimitedGoals")).isTrue();
        }

        @Test
        @DisplayName("Should return free entitlements")
        void shouldReturnFreeEntitlements() {
            when(subscriptionRepository.findActiveSubscription(user)).thenReturn(Optional.empty());
            when(purchaseRepository.countAvailable(eq(user), any(), any())).thenReturn(0);

            var result = subscriptionService.getEntitlements(user);

            assertThat(result.getTier()).isEqualTo(SubscriptionTier.FREE);
            assertThat(result.getIsPremium()).isFalse();
            assertThat(result.getMaxActiveGoals()).isEqualTo(2);
            assertThat(result.getMaxGuardiansPerGoal()).isEqualTo(1);
            assertThat(result.getProgressHistoryDays()).isEqualTo(30);
            assertThat(result.getStreakShieldsPerMonth()).isEqualTo(1);
            assertThat(result.getStrugglingRequestsPerMonth()).isEqualTo(1);
            assertThat(result.getFeatures().get("unlimitedGoals")).isFalse();
            assertThat(result.getFeatures().get("weeklyReflections")).isTrue();
        }

        @Test
        @DisplayName("Should include purchased items in entitlements")
        void shouldIncludePurchasedItemsInEntitlements() {
            when(subscriptionRepository.findActiveSubscription(user)).thenReturn(Optional.empty());
            when(purchaseRepository.countAvailable(eq(user), eq(PurchaseType.STREAK_SHIELD), any(LocalDateTime.class))).thenReturn(5);
            when(purchaseRepository.countAvailable(eq(user), eq(PurchaseType.STRUGGLING_ASSIST), any(LocalDateTime.class))).thenReturn(2);
            when(purchaseRepository.countAvailable(eq(user), eq(PurchaseType.GOAL_BOOST), any(LocalDateTime.class))).thenReturn(1);
            when(purchaseRepository.countAvailable(eq(user), eq(PurchaseType.GUARDIAN_SLOT), any(LocalDateTime.class))).thenReturn(3);

            var result = subscriptionService.getEntitlements(user);

            assertThat(result.getStreakShieldsAvailable()).isEqualTo(5);
            assertThat(result.getStrugglingAssistsAvailable()).isEqualTo(2);
            assertThat(result.getGoalBoostsAvailable()).isEqualTo(1);
            assertThat(result.getGuardianSlotsAvailable()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Get purchases tests")
    class GetPurchasesTests {

        @Test
        @DisplayName("Should return all purchases")
        void shouldReturnAllPurchases() {
            when(purchaseRepository.findByUserOrderByPurchasedAtDesc(user)).thenReturn(List.of(purchase));

            var result = subscriptionService.getPurchases(user);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPurchaseType()).isEqualTo(PurchaseType.STREAK_SHIELD);
            assertThat(result.get(0).getQuantity()).isEqualTo(3);
            assertThat(result.get(0).getQuantityRemaining()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should return empty list when no purchases")
        void shouldReturnEmptyListWhenNoPurchases() {
            when(purchaseRepository.findByUserOrderByPurchasedAtDesc(user)).thenReturn(List.of());

            var result = subscriptionService.getPurchases(user);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Is premium tests")
    class IsPremiumTests {

        @Test
        @DisplayName("Should return true for premium user")
        void shouldReturnTrueForPremiumUser() {
            when(subscriptionRepository.findActiveSubscription(user)).thenReturn(Optional.of(premiumSubscription));

            var result = subscriptionService.isPremium(user);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false for free user")
        void shouldReturnFalseForFreeUser() {
            when(subscriptionRepository.findActiveSubscription(user)).thenReturn(Optional.empty());

            var result = subscriptionService.isPremium(user);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for free tier subscription")
        void shouldReturnFalseForFreeTierSubscription() {
            var freeSubscription = UserSubscription.builder()
                    .tier(SubscriptionTier.FREE)
                    .status(SubscriptionStatus.ACTIVE)
                    .build();

            when(subscriptionRepository.findActiveSubscription(user)).thenReturn(Optional.of(freeSubscription));

            var result = subscriptionService.isPremium(user);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Get available purchase count tests")
    class GetAvailablePurchaseCountTests {

        @Test
        @DisplayName("Should return available purchase count")
        void shouldReturnAvailablePurchaseCount() {
            when(purchaseRepository.countAvailable(eq(user), eq(PurchaseType.STREAK_SHIELD), any()))
                    .thenReturn(5);

            var result = subscriptionService.getAvailablePurchaseCount(user, PurchaseType.STREAK_SHIELD);

            assertThat(result).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Consume purchase tests")
    class ConsumePurchaseTests {

        @Test
        @DisplayName("Should consume purchase successfully")
        void shouldConsumePurchaseSuccessfully() {
            when(purchaseRepository.findAvailablePurchases(eq(user), eq(PurchaseType.STREAK_SHIELD), any()))
                    .thenReturn(List.of(purchase));

            var result = subscriptionService.consumePurchase(user, PurchaseType.STREAK_SHIELD);

            assertThat(result).isTrue();
            verify(purchaseRepository).save(purchase);
            assertThat(purchase.getQuantityRemaining()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return false when no purchases available")
        void shouldReturnFalseWhenNoPurchasesAvailable() {
            when(purchaseRepository.findAvailablePurchases(eq(user), eq(PurchaseType.STREAK_SHIELD), any()))
                    .thenReturn(List.of());

            var result = subscriptionService.consumePurchase(user, PurchaseType.STREAK_SHIELD);

            assertThat(result).isFalse();
            verify(purchaseRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should consume from first available purchase")
        void shouldConsumeFromFirstAvailablePurchase() {
            var firstPurchase = UserPurchase.builder()
                    .id(1L)
                    .purchaseType(PurchaseType.STREAK_SHIELD)
                    .quantity(3)
                    .quantityRemaining(1)
                    .build();
            var secondPurchase = UserPurchase.builder()
                    .id(2L)
                    .purchaseType(PurchaseType.STREAK_SHIELD)
                    .quantity(3)
                    .quantityRemaining(3)
                    .build();

            when(purchaseRepository.findAvailablePurchases(eq(user), eq(PurchaseType.STREAK_SHIELD), any()))
                    .thenReturn(List.of(firstPurchase, secondPurchase));

            var result = subscriptionService.consumePurchase(user, PurchaseType.STREAK_SHIELD);

            assertThat(result).isTrue();
            verify(purchaseRepository).save(firstPurchase);
            assertThat(firstPurchase.getQuantityRemaining()).isZero();
        }
    }
}
