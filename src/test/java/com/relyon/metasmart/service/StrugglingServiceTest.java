package com.relyon.metasmart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.goal.GoalCategory;
import com.relyon.metasmart.entity.guardian.GoalGuardian;
import com.relyon.metasmart.entity.guardian.GuardianPermission;
import com.relyon.metasmart.entity.guardian.GuardianStatus;
import com.relyon.metasmart.entity.struggling.StrugglingRequest;
import com.relyon.metasmart.entity.struggling.StrugglingType;
import com.relyon.metasmart.entity.struggling.dto.StrugglingHelpRequest;
import com.relyon.metasmart.entity.subscription.PurchaseType;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.BadRequestException;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.repository.GoalGuardianRepository;
import com.relyon.metasmart.repository.GoalRepository;
import com.relyon.metasmart.repository.GuardianNudgeRepository;
import com.relyon.metasmart.repository.StrugglingRequestRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class StrugglingServiceTest {

    @Mock
    private StrugglingRequestRepository strugglingRequestRepository;

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private GoalGuardianRepository goalGuardianRepository;

    @Mock
    private GuardianNudgeRepository guardianNudgeRepository;

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private StrugglingService strugglingService;

    private User user;
    private Goal goal;
    private StrugglingRequest strugglingRequest;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("John").email("john@test.com").build();
        goal = Goal.builder()
                .id(1L)
                .title("Run 5K")
                .owner(user)
                .goalCategory(GoalCategory.HEALTH)
                .targetValue(new BigDecimal("100"))
                .targetDate(LocalDate.now().plusDays(30))
                .build();
        strugglingRequest = StrugglingRequest.builder()
                .id(1L)
                .goal(goal)
                .user(user)
                .strugglingType(StrugglingType.LACK_OF_TIME)
                .userMessage("Need help")
                .aiSuggestion("Try breaking into smaller tasks")
                .notifyGuardians(false)
                .guardiansNotified(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Get status tests")
    class GetStatusTests {

        @Test
        @DisplayName("Should get status with free requests available")
        void shouldGetStatusWithFreeRequestsAvailable() {
            when(strugglingRequestRepository.countByUserThisMonth(eq(user), any())).thenReturn(0L);
            when(subscriptionService.isPremium(user)).thenReturn(false);
            when(subscriptionService.getAvailablePurchaseCount(user, PurchaseType.STRUGGLING_ASSIST)).thenReturn(0);

            var result = strugglingService.getStatus(user);

            assertThat(result.getFreeRequestsUsedThisMonth()).isZero();
            assertThat(result.getFreeRequestsRemaining()).isEqualTo(1);
            assertThat(result.getCanRequestHelp()).isTrue();
        }

        @Test
        @DisplayName("Should get status with no free requests remaining")
        void shouldGetStatusWithNoFreeRequestsRemaining() {
            when(strugglingRequestRepository.countByUserThisMonth(eq(user), any())).thenReturn(1L);
            when(subscriptionService.isPremium(user)).thenReturn(false);
            when(subscriptionService.getAvailablePurchaseCount(user, PurchaseType.STRUGGLING_ASSIST)).thenReturn(0);

            var result = strugglingService.getStatus(user);

            assertThat(result.getFreeRequestsRemaining()).isZero();
            assertThat(result.getCanRequestHelp()).isFalse();
        }

        @Test
        @DisplayName("Should get status for premium user")
        void shouldGetStatusForPremiumUser() {
            when(strugglingRequestRepository.countByUserThisMonth(eq(user), any())).thenReturn(5L);
            when(subscriptionService.isPremium(user)).thenReturn(true);

            var result = strugglingService.getStatus(user);

            assertThat(result.getPaidRequestsAvailable()).isEqualTo(Integer.MAX_VALUE);
            assertThat(result.getCanRequestHelp()).isTrue();
        }
    }

    @Nested
    @DisplayName("Request help tests")
    class RequestHelpTests {

        @Test
        @DisplayName("Should request help successfully")
        void shouldRequestHelpSuccessfully() {
            var request = StrugglingHelpRequest.builder()
                    .strugglingType(StrugglingType.LACK_OF_TIME)
                    .message("I'm too busy")
                    .notifyGuardians(false)
                    .build();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(strugglingRequestRepository.countByUserThisMonth(eq(user), any())).thenReturn(0L);
            when(subscriptionService.isPremium(user)).thenReturn(false);
            when(subscriptionService.getAvailablePurchaseCount(user, PurchaseType.STRUGGLING_ASSIST)).thenReturn(0);
            when(strugglingRequestRepository.save(any())).thenReturn(strugglingRequest);

            var result = strugglingService.requestHelp(1L, request, user);

            assertThat(result).isNotNull();
            assertThat(result.getGoalId()).isEqualTo(1L);
            assertThat(result.getSuggestions()).isNotEmpty();
            verify(strugglingRequestRepository).save(any());
        }

        @Test
        @DisplayName("Should throw when goal not found")
        void shouldThrowWhenGoalNotFound() {
            var request = StrugglingHelpRequest.builder()
                    .strugglingType(StrugglingType.LACK_OF_TIME)
                    .build();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> strugglingService.requestHelp(1L, request, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.GOAL_NOT_FOUND);
        }

        @Test
        @DisplayName("Should throw when limit reached")
        void shouldThrowWhenLimitReached() {
            var request = StrugglingHelpRequest.builder()
                    .strugglingType(StrugglingType.LACK_OF_TIME)
                    .build();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(strugglingRequestRepository.countByUserThisMonth(eq(user), any())).thenReturn(1L);
            when(subscriptionService.isPremium(user)).thenReturn(false);
            when(subscriptionService.getAvailablePurchaseCount(user, PurchaseType.STRUGGLING_ASSIST)).thenReturn(0);

            assertThatThrownBy(() -> strugglingService.requestHelp(1L, request, user))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage(ErrorMessages.STRUGGLING_LIMIT_REACHED);
        }

        @Test
        @DisplayName("Should notify guardians when requested")
        void shouldNotifyGuardiansWhenRequested() {
            var guardian = User.builder().id(2L).name("Guardian").build();
            var goalGuardian = GoalGuardian.builder()
                    .id(1L)
                    .goal(goal)
                    .guardian(guardian)
                    .status(GuardianStatus.ACTIVE)
                    .permissions(Set.of(GuardianPermission.SEND_NUDGE))
                    .build();

            var request = StrugglingHelpRequest.builder()
                    .strugglingType(StrugglingType.LACK_OF_MOTIVATION)
                    .message("Need encouragement")
                    .notifyGuardians(true)
                    .build();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(strugglingRequestRepository.countByUserThisMonth(eq(user), any())).thenReturn(0L);
            when(subscriptionService.isPremium(user)).thenReturn(false);
            when(subscriptionService.getAvailablePurchaseCount(user, PurchaseType.STRUGGLING_ASSIST)).thenReturn(0);
            when(strugglingRequestRepository.save(any())).thenReturn(strugglingRequest);
            when(goalGuardianRepository.findByGoalAndStatus(goal, GuardianStatus.ACTIVE))
                    .thenReturn(List.of(goalGuardian));

            var result = strugglingService.requestHelp(1L, request, user);

            assertThat(result.getGuardiansNotified()).isTrue();
            verify(guardianNudgeRepository).save(any());
        }

        @Test
        @DisplayName("Should generate suggestions for all struggling types")
        void shouldGenerateSuggestionsForAllStrugglingTypes() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(strugglingRequestRepository.countByUserThisMonth(eq(user), any())).thenReturn(0L);
            when(subscriptionService.isPremium(user)).thenReturn(false);
            when(subscriptionService.getAvailablePurchaseCount(user, PurchaseType.STRUGGLING_ASSIST)).thenReturn(0);
            when(strugglingRequestRepository.save(any())).thenReturn(strugglingRequest);

            for (StrugglingType type : StrugglingType.values()) {
                var request = StrugglingHelpRequest.builder()
                        .strugglingType(type)
                        .message("Help needed")
                        .notifyGuardians(false)
                        .build();

                var result = strugglingService.requestHelp(1L, request, user);

                assertThat(result.getSuggestions()).isNotEmpty();
            }
        }
    }

    @Nested
    @DisplayName("Get history tests")
    class GetHistoryTests {

        @Test
        @DisplayName("Should get history")
        void shouldGetHistory() {
            var pageable = Pageable.ofSize(10);
            var page = new PageImpl<>(List.of(strugglingRequest));

            when(strugglingRequestRepository.findByUserOrderByCreatedAtDesc(user, pageable))
                    .thenReturn(page);

            var result = strugglingService.getHistory(user, pageable);

            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Mark helpful tests")
    class MarkHelpfulTests {

        @Test
        @DisplayName("Should mark as helpful")
        void shouldMarkAsHelpful() {
            when(strugglingRequestRepository.findById(1L)).thenReturn(Optional.of(strugglingRequest));

            strugglingService.markHelpful(1L, true, user);

            verify(strugglingRequestRepository).save(argThat(r -> r.getWasHelpful()));
        }

        @Test
        @DisplayName("Should mark as not helpful")
        void shouldMarkAsNotHelpful() {
            when(strugglingRequestRepository.findById(1L)).thenReturn(Optional.of(strugglingRequest));

            strugglingService.markHelpful(1L, false, user);

            verify(strugglingRequestRepository).save(argThat(r -> !r.getWasHelpful()));
        }

        @Test
        @DisplayName("Should throw when request not found")
        void shouldThrowWhenRequestNotFound() {
            when(strugglingRequestRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> strugglingService.markHelpful(1L, true, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.STRUGGLING_REQUEST_NOT_FOUND);
        }

        @Test
        @DisplayName("Should throw when request belongs to different user")
        void shouldThrowWhenRequestBelongsToDifferentUser() {
            var otherUser = User.builder().id(2L).build();
            strugglingRequest.setUser(otherUser);

            when(strugglingRequestRepository.findById(1L)).thenReturn(Optional.of(strugglingRequest));

            assertThatThrownBy(() -> strugglingService.markHelpful(1L, true, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.STRUGGLING_REQUEST_NOT_FOUND);
        }
    }
}
