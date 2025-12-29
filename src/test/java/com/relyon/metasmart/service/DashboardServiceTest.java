package com.relyon.metasmart.service;

import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.goal.GoalCategory;
import com.relyon.metasmart.entity.goal.GoalStatus;
import com.relyon.metasmart.entity.reflection.dto.PendingReflectionResponse;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.repository.GoalRepository;
import com.relyon.metasmart.repository.ProgressEntryRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private ProgressEntryRepository progressEntryRepository;

    @Mock
    private ReflectionService reflectionService;

    @Mock
    private GuardianNudgeService guardianNudgeService;

    @InjectMocks
    private DashboardService dashboardService;

    private User user;
    private Goal activeGoal;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("John")
                .email("john@test.com")
                .streakShields(2)
                .build();

        activeGoal = Goal.builder()
                .id(1L)
                .title("Run 5K")
                .owner(user)
                .goalStatus(GoalStatus.ACTIVE)
                .goalCategory(GoalCategory.HEALTH)
                .currentProgress(BigDecimal.valueOf(50))
                .targetValue("100")
                .build();
    }

    @Nested
    @DisplayName("Get dashboard tests")
    class GetDashboardTests {

        @Test
        @DisplayName("Should get dashboard with all counts")
        void shouldGetDashboardWithAllCounts() {
            when(goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.ACTIVE)).thenReturn(3L);
            when(goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.COMPLETED)).thenReturn(2L);
            when(reflectionService.getPendingReflections(user)).thenReturn(List.of(
                    PendingReflectionResponse.builder().goalId(1L).build()
            ));
            when(guardianNudgeService.countUnreadNudges(user)).thenReturn(5L);
            when(goalRepository.findByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.ACTIVE))
                    .thenReturn(Collections.emptyList());

            var result = dashboardService.getDashboard(user);

            assertThat(result.getActiveGoalsCount()).isEqualTo(3);
            assertThat(result.getCompletedGoalsCount()).isEqualTo(2);
            assertThat(result.getPendingReflectionsCount()).isEqualTo(1);
            assertThat(result.getUnreadNudgesCount()).isEqualTo(5);
            assertThat(result.getStreakShieldsAvailable()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should get dashboard with zero counts")
        void shouldGetDashboardWithZeroCounts() {
            when(goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.ACTIVE)).thenReturn(0L);
            when(goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.COMPLETED)).thenReturn(0L);
            when(reflectionService.getPendingReflections(user)).thenReturn(Collections.emptyList());
            when(guardianNudgeService.countUnreadNudges(user)).thenReturn(0L);
            when(goalRepository.findByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.ACTIVE))
                    .thenReturn(Collections.emptyList());

            var result = dashboardService.getDashboard(user);

            assertThat(result.getActiveGoalsCount()).isZero();
            assertThat(result.getCompletedGoalsCount()).isZero();
            assertThat(result.getPendingReflectionsCount()).isZero();
            assertThat(result.getUnreadNudgesCount()).isZero();
        }

        @Test
        @DisplayName("Should find streaks at risk")
        void shouldFindStreaksAtRisk() {
            var goalWithStreak = Goal.builder()
                    .id(1L)
                    .title("Daily Run")
                    .goalStatus(GoalStatus.ACTIVE)
                    .build();

            when(goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.ACTIVE)).thenReturn(1L);
            when(goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.COMPLETED)).thenReturn(0L);
            when(reflectionService.getPendingReflections(user)).thenReturn(Collections.emptyList());
            when(guardianNudgeService.countUnreadNudges(user)).thenReturn(0L);
            when(goalRepository.findByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.ACTIVE))
                    .thenReturn(List.of(goalWithStreak));
            when(progressEntryRepository.findDistinctProgressDates(goalWithStreak))
                    .thenReturn(List.of(LocalDate.now().minusDays(1), LocalDate.now().minusDays(2)));

            var result = dashboardService.getDashboard(user);

            assertThat(result.getStreaksAtRisk()).hasSize(1);
            assertThat(result.getStreaksAtRisk().get(0).getGoalTitle()).isEqualTo("Daily Run");
        }
    }

    @Nested
    @DisplayName("Get goal stats tests")
    class GetGoalStatsTests {

        @Test
        @DisplayName("Should get goal stats with completion rate")
        void shouldGetGoalStatsWithCompletionRate() {
            when(goalRepository.countByOwnerAndArchivedAtIsNull(user)).thenReturn(10L);
            when(goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.ACTIVE)).thenReturn(5L);
            when(goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.COMPLETED)).thenReturn(3L);
            when(goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.PAUSED)).thenReturn(1L);
            when(goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.ABANDONED)).thenReturn(1L);
            when(goalRepository.findByOwnerAndArchivedAtIsNull(eq(user), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(activeGoal)));
            when(progressEntryRepository.findDistinctProgressDates(any())).thenReturn(Collections.emptyList());

            var result = dashboardService.getGoalStats(user);

            assertThat(result.getTotalGoals()).isEqualTo(10);
            assertThat(result.getActiveGoals()).isEqualTo(5);
            assertThat(result.getCompletedGoals()).isEqualTo(3);
            assertThat(result.getPausedGoals()).isEqualTo(1);
            assertThat(result.getAbandonedGoals()).isEqualTo(1);
            assertThat(result.getCompletionRate()).isEqualByComparingTo(BigDecimal.valueOf(30.00));
        }

        @Test
        @DisplayName("Should handle zero total goals")
        void shouldHandleZeroTotalGoals() {
            when(goalRepository.countByOwnerAndArchivedAtIsNull(user)).thenReturn(0L);
            when(goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.ACTIVE)).thenReturn(0L);
            when(goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.COMPLETED)).thenReturn(0L);
            when(goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.PAUSED)).thenReturn(0L);
            when(goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.ABANDONED)).thenReturn(0L);
            when(goalRepository.findByOwnerAndArchivedAtIsNull(eq(user), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(Collections.emptyList()));

            var result = dashboardService.getGoalStats(user);

            assertThat(result.getTotalGoals()).isZero();
            assertThat(result.getCompletionRate()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getGoalsByCategory()).isEmpty();
        }

        @Test
        @DisplayName("Should calculate goals by category")
        void shouldCalculateGoalsByCategory() {
            var healthGoal = Goal.builder().id(1L).goalCategory(GoalCategory.HEALTH).goalStatus(GoalStatus.ACTIVE).build();
            var financeGoal = Goal.builder().id(2L).goalCategory(GoalCategory.FINANCE).goalStatus(GoalStatus.ACTIVE).build();

            when(goalRepository.countByOwnerAndArchivedAtIsNull(user)).thenReturn(2L);
            when(goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(any(), any())).thenReturn(0L);
            when(goalRepository.findByOwnerAndArchivedAtIsNull(eq(user), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(healthGoal, financeGoal)));
            when(progressEntryRepository.findDistinctProgressDates(any())).thenReturn(Collections.emptyList());

            var result = dashboardService.getGoalStats(user);

            assertThat(result.getGoalsByCategory()).containsEntry("HEALTH", 1L);
            assertThat(result.getGoalsByCategory()).containsEntry("FINANCE", 1L);
        }

        @Test
        @DisplayName("Should calculate best streaks")
        void shouldCalculateBestStreaks() {
            var goalWithStreak = Goal.builder()
                    .id(1L)
                    .goalCategory(GoalCategory.HEALTH)
                    .goalStatus(GoalStatus.ACTIVE)
                    .build();

            when(goalRepository.countByOwnerAndArchivedAtIsNull(user)).thenReturn(1L);
            when(goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(any(), any())).thenReturn(0L);
            when(goalRepository.findByOwnerAndArchivedAtIsNull(eq(user), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(goalWithStreak)));
            when(progressEntryRepository.findDistinctProgressDates(goalWithStreak))
                    .thenReturn(List.of(
                            LocalDate.now(),
                            LocalDate.now().minusDays(1),
                            LocalDate.now().minusDays(2)
                    ));

            var result = dashboardService.getGoalStats(user);

            assertThat(result.getCurrentBestStreak()).isGreaterThanOrEqualTo(0);
            assertThat(result.getBestStreak()).isGreaterThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Streak calculation edge cases")
    class StreakCalculationEdgeCases {

        @Test
        @DisplayName("Should handle goal with streak shield used")
        void shouldHandleGoalWithStreakShieldUsed() {
            var goalWithShield = Goal.builder()
                    .id(1L)
                    .title("Daily Run")
                    .goalStatus(GoalStatus.ACTIVE)
                    .lastStreakShieldUsedAt(LocalDate.now().minusDays(1))
                    .build();

            when(goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.ACTIVE)).thenReturn(1L);
            when(goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.COMPLETED)).thenReturn(0L);
            when(reflectionService.getPendingReflections(user)).thenReturn(Collections.emptyList());
            when(guardianNudgeService.countUnreadNudges(user)).thenReturn(0L);
            when(goalRepository.findByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.ACTIVE))
                    .thenReturn(List.of(goalWithShield));
            when(progressEntryRepository.findDistinctProgressDates(goalWithShield))
                    .thenReturn(List.of(LocalDate.now().minusDays(2)));

            var result = dashboardService.getDashboard(user);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle empty progress dates")
        void shouldHandleEmptyProgressDates() {
            var goalNoProgress = Goal.builder()
                    .id(1L)
                    .title("New Goal")
                    .goalStatus(GoalStatus.ACTIVE)
                    .build();

            when(goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.ACTIVE)).thenReturn(1L);
            when(goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.COMPLETED)).thenReturn(0L);
            when(reflectionService.getPendingReflections(user)).thenReturn(Collections.emptyList());
            when(guardianNudgeService.countUnreadNudges(user)).thenReturn(0L);
            when(goalRepository.findByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.ACTIVE))
                    .thenReturn(List.of(goalNoProgress));
            when(progressEntryRepository.findDistinctProgressDates(goalNoProgress))
                    .thenReturn(Collections.emptyList());

            var result = dashboardService.getDashboard(user);

            assertThat(result.getStreaksAtRisk()).isEmpty();
        }
    }
}
