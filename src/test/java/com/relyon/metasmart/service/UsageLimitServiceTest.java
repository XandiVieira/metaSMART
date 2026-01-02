package com.relyon.metasmart.service;

import com.relyon.metasmart.entity.goal.GoalStatus;
import com.relyon.metasmart.entity.guardian.GuardianStatus;
import com.relyon.metasmart.entity.subscription.SubscriptionTier;
import com.relyon.metasmart.entity.subscription.dto.UserEntitlementsResponse;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.UsageLimitExceededException;
import com.relyon.metasmart.repository.GoalGuardianRepository;
import com.relyon.metasmart.repository.GoalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsageLimitServiceTest {

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private GoalGuardianRepository goalGuardianRepository;

    @InjectMocks
    private UsageLimitService usageLimitService;

    private User freeUser;
    private User premiumUser;
    private UserEntitlementsResponse freeEntitlements;
    private UserEntitlementsResponse premiumEntitlements;

    @BeforeEach
    void setUp() {
        freeUser = User.builder()
                .id(1L)
                .email("free@example.com")
                .name("Free User")
                .build();

        premiumUser = User.builder()
                .id(2L)
                .email("premium@example.com")
                .name("Premium User")
                .build();

        freeEntitlements = UserEntitlementsResponse.builder()
                .tier(SubscriptionTier.FREE)
                .isPremium(false)
                .maxActiveGoals(3)
                .maxGuardiansPerGoal(1)
                .progressHistoryDays(30)
                .streakShieldsPerMonth(1)
                .strugglingRequestsPerMonth(1)
                .features(new HashMap<>())
                .build();

        premiumEntitlements = UserEntitlementsResponse.builder()
                .tier(SubscriptionTier.PREMIUM)
                .isPremium(true)
                .maxActiveGoals(Integer.MAX_VALUE)
                .maxGuardiansPerGoal(5)
                .progressHistoryDays(Integer.MAX_VALUE)
                .streakShieldsPerMonth(3)
                .strugglingRequestsPerMonth(Integer.MAX_VALUE)
                .features(new HashMap<>())
                .build();
    }

    @Nested
    @DisplayName("Enforce goal limit tests")
    class EnforceGoalLimitTests {

        @Test
        @DisplayName("Should allow goal creation when under limit")
        void shouldAllowGoalCreationWhenUnderLimit() {
            when(subscriptionService.getEntitlements(freeUser)).thenReturn(freeEntitlements);
            when(goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(freeUser, GoalStatus.ACTIVE))
                    .thenReturn(2L);

            usageLimitService.enforceGoalLimit(freeUser);
        }

        @Test
        @DisplayName("Should throw when goal limit exceeded for free user")
        void shouldThrowWhenGoalLimitExceededForFreeUser() {
            when(subscriptionService.getEntitlements(freeUser)).thenReturn(freeEntitlements);
            when(goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(freeUser, GoalStatus.ACTIVE))
                    .thenReturn(3L);

            assertThatThrownBy(() -> usageLimitService.enforceGoalLimit(freeUser))
                    .isInstanceOf(UsageLimitExceededException.class)
                    .hasMessageContaining("active goals")
                    .hasMessageContaining("3/3");
        }

        @Test
        @DisplayName("Should allow many goals for premium user")
        void shouldAllowManyGoalsForPremiumUser() {
            when(subscriptionService.getEntitlements(premiumUser)).thenReturn(premiumEntitlements);
            when(goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(premiumUser, GoalStatus.ACTIVE))
                    .thenReturn(100L);

            usageLimitService.enforceGoalLimit(premiumUser);
        }

        @Test
        @DisplayName("Should allow goal creation when at zero goals")
        void shouldAllowGoalCreationWhenAtZeroGoals() {
            when(subscriptionService.getEntitlements(freeUser)).thenReturn(freeEntitlements);
            when(goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(freeUser, GoalStatus.ACTIVE))
                    .thenReturn(0L);

            usageLimitService.enforceGoalLimit(freeUser);
        }
    }

    @Nested
    @DisplayName("Enforce guardian limit tests")
    class EnforceGuardianLimitTests {

        @Test
        @DisplayName("Should allow guardian invitation when under limit")
        void shouldAllowGuardianInvitationWhenUnderLimit() {
            when(subscriptionService.getEntitlements(freeUser)).thenReturn(freeEntitlements);
            when(goalGuardianRepository.countByGoalAndStatus(any(), eq(GuardianStatus.ACTIVE)))
                    .thenReturn(0L);
            when(goalGuardianRepository.countByGoalAndStatus(any(), eq(GuardianStatus.PENDING)))
                    .thenReturn(0L);

            usageLimitService.enforceGuardianLimit(freeUser, 1L);
        }

        @Test
        @DisplayName("Should throw when guardian limit exceeded for free user")
        void shouldThrowWhenGuardianLimitExceededForFreeUser() {
            when(subscriptionService.getEntitlements(freeUser)).thenReturn(freeEntitlements);
            when(goalGuardianRepository.countByGoalAndStatus(any(), eq(GuardianStatus.ACTIVE)))
                    .thenReturn(1L);
            when(goalGuardianRepository.countByGoalAndStatus(any(), eq(GuardianStatus.PENDING)))
                    .thenReturn(0L);

            assertThatThrownBy(() -> usageLimitService.enforceGuardianLimit(freeUser, 1L))
                    .isInstanceOf(UsageLimitExceededException.class)
                    .hasMessageContaining("guardians per goal")
                    .hasMessageContaining("1/1");
        }

        @Test
        @DisplayName("Should count pending guardians toward limit")
        void shouldCountPendingGuardiansTowardLimit() {
            when(subscriptionService.getEntitlements(freeUser)).thenReturn(freeEntitlements);
            when(goalGuardianRepository.countByGoalAndStatus(any(), eq(GuardianStatus.ACTIVE)))
                    .thenReturn(0L);
            when(goalGuardianRepository.countByGoalAndStatus(any(), eq(GuardianStatus.PENDING)))
                    .thenReturn(1L);

            assertThatThrownBy(() -> usageLimitService.enforceGuardianLimit(freeUser, 1L))
                    .isInstanceOf(UsageLimitExceededException.class);
        }

        @Test
        @DisplayName("Should allow multiple guardians for premium user")
        void shouldAllowMultipleGuardiansForPremiumUser() {
            when(subscriptionService.getEntitlements(premiumUser)).thenReturn(premiumEntitlements);
            when(goalGuardianRepository.countByGoalAndStatus(any(), eq(GuardianStatus.ACTIVE)))
                    .thenReturn(3L);
            when(goalGuardianRepository.countByGoalAndStatus(any(), eq(GuardianStatus.PENDING)))
                    .thenReturn(1L);

            usageLimitService.enforceGuardianLimit(premiumUser, 1L);
        }
    }

    @Nested
    @DisplayName("Get remaining limits tests")
    class GetRemainingLimitsTests {

        @Test
        @DisplayName("Should return correct remaining goals for free user")
        void shouldReturnCorrectRemainingGoalsForFreeUser() {
            when(subscriptionService.getEntitlements(freeUser)).thenReturn(freeEntitlements);
            when(goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(freeUser, GoalStatus.ACTIVE))
                    .thenReturn(1L);

            var remaining = usageLimitService.getRemainingGoals(freeUser);

            assertThat(remaining).isEqualTo(2);
        }

        @Test
        @DisplayName("Should return zero remaining when at limit")
        void shouldReturnZeroRemainingWhenAtLimit() {
            when(subscriptionService.getEntitlements(freeUser)).thenReturn(freeEntitlements);
            when(goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(freeUser, GoalStatus.ACTIVE))
                    .thenReturn(3L);

            var remaining = usageLimitService.getRemainingGoals(freeUser);

            assertThat(remaining).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return zero remaining when over limit")
        void shouldReturnZeroRemainingWhenOverLimit() {
            when(subscriptionService.getEntitlements(freeUser)).thenReturn(freeEntitlements);
            when(goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(freeUser, GoalStatus.ACTIVE))
                    .thenReturn(5L);

            var remaining = usageLimitService.getRemainingGoals(freeUser);

            assertThat(remaining).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return correct remaining guardians")
        void shouldReturnCorrectRemainingGuardians() {
            when(subscriptionService.getEntitlements(freeUser)).thenReturn(freeEntitlements);
            when(goalGuardianRepository.countByGoalAndStatus(any(), eq(GuardianStatus.ACTIVE)))
                    .thenReturn(0L);
            when(goalGuardianRepository.countByGoalAndStatus(any(), eq(GuardianStatus.PENDING)))
                    .thenReturn(0L);

            var remaining = usageLimitService.getRemainingGuardians(freeUser, 1L);

            assertThat(remaining).isEqualTo(1);
        }
    }
}
