package com.relyon.metasmart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.goal.GoalCategory;
import com.relyon.metasmart.entity.struggling.StrugglingType;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.repository.GoalRepository;
import com.relyon.metasmart.repository.ProgressEntryRepository;
import com.relyon.metasmart.repository.StrugglingRequestRepository;
import java.math.BigDecimal;
import java.util.Collections;
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
class SocialProofServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private ProgressEntryRepository progressEntryRepository;

    @Mock
    private StrugglingRequestRepository strugglingRequestRepository;

    @InjectMocks
    private SocialProofService socialProofService;

    private User user;
    private Goal goal;

    @BeforeEach
    void setUp() {
        socialProofService.setSelf(socialProofService);

        user = User.builder().id(1L).name("John").email("john@test.com").build();
        goal = Goal.builder()
                .id(1L)
                .title("Run 5K")
                .owner(user)
                .goalCategory(GoalCategory.HEALTH)
                .targetValue(new BigDecimal("100"))
                .currentProgress(BigDecimal.valueOf(50))
                .build();
    }

    @Nested
    @DisplayName("Get global stats tests")
    class GetGlobalStatsTests {

        @Test
        @DisplayName("Should get global stats with data")
        void shouldGetGlobalStatsWithData() {
            when(goalRepository.countDistinctUsers()).thenReturn(100L);
            when(goalRepository.count()).thenReturn(500L);
            when(goalRepository.countCompletedGoals()).thenReturn(200L);
            when(progressEntryRepository.countAllProgressEntries()).thenReturn(10000L);
            when(goalRepository.countByCategory(GoalCategory.HEALTH)).thenReturn(100L);
            when(goalRepository.countCompletedByCategory(GoalCategory.HEALTH)).thenReturn(40L);
            when(goalRepository.countByCategory(GoalCategory.FINANCE)).thenReturn(80L);
            when(goalRepository.countCompletedByCategory(GoalCategory.FINANCE)).thenReturn(30L);
            when(goalRepository.countByCategory(GoalCategory.EDUCATION)).thenReturn(60L);
            when(goalRepository.countCompletedByCategory(GoalCategory.EDUCATION)).thenReturn(25L);
            when(goalRepository.countByCategory(GoalCategory.CAREER)).thenReturn(50L);
            when(goalRepository.countCompletedByCategory(GoalCategory.CAREER)).thenReturn(20L);
            when(goalRepository.countByCategory(GoalCategory.RELATIONSHIPS)).thenReturn(40L);
            when(goalRepository.countCompletedByCategory(GoalCategory.RELATIONSHIPS)).thenReturn(15L);
            when(goalRepository.countByCategory(GoalCategory.PERSONAL_DEVELOPMENT)).thenReturn(70L);
            when(goalRepository.countCompletedByCategory(GoalCategory.PERSONAL_DEVELOPMENT)).thenReturn(28L);
            when(goalRepository.countByCategory(GoalCategory.HOBBIES)).thenReturn(50L);
            when(goalRepository.countCompletedByCategory(GoalCategory.HOBBIES)).thenReturn(22L);
            when(goalRepository.countByCategory(GoalCategory.OTHER)).thenReturn(50L);
            when(goalRepository.countCompletedByCategory(GoalCategory.OTHER)).thenReturn(20L);

            var result = socialProofService.getGlobalStats();

            assertThat(result.getTotalActiveUsers()).isEqualTo(100);
            assertThat(result.getTotalGoalsCreated()).isEqualTo(500);
            assertThat(result.getTotalGoalsCompleted()).isEqualTo(200);
            assertThat(result.getOverallCompletionRate()).isEqualTo(40.0);
            assertThat(result.getTotalProgressEntries()).isEqualTo(10000);
            assertThat(result.getGoalsByCategory()).containsEntry("HEALTH", 100L);
        }

        @Test
        @DisplayName("Should handle zero total goals")
        void shouldHandleZeroTotalGoals() {
            when(goalRepository.countDistinctUsers()).thenReturn(0L);
            when(goalRepository.count()).thenReturn(0L);
            when(goalRepository.countCompletedGoals()).thenReturn(0L);
            when(progressEntryRepository.countAllProgressEntries()).thenReturn(0L);
            for (GoalCategory category : GoalCategory.values()) {
                when(goalRepository.countByCategory(category)).thenReturn(0L);
                when(goalRepository.countCompletedByCategory(category)).thenReturn(0L);
            }

            var result = socialProofService.getGlobalStats();

            assertThat(result.getOverallCompletionRate()).isZero();
        }
    }

    @Nested
    @DisplayName("Get category stats tests")
    class GetCategoryStatsTests {

        @Test
        @DisplayName("Should get category stats for health")
        void shouldGetCategoryStatsForHealth() {
            when(goalRepository.countDistinctUsersByCategory(GoalCategory.HEALTH)).thenReturn(50L);
            when(goalRepository.countActiveByCatgory(GoalCategory.HEALTH)).thenReturn(30L);
            when(goalRepository.countCompletedByCategory(GoalCategory.HEALTH)).thenReturn(20L);
            when(goalRepository.countByCategory(GoalCategory.HEALTH)).thenReturn(50L);
            when(strugglingRequestRepository.findTopStrugglingTypesByCategory(GoalCategory.HEALTH))
                    .thenReturn(Collections.singletonList(new Object[]{StrugglingType.LACK_OF_TIME}));

            var result = socialProofService.getCategoryStats(GoalCategory.HEALTH);

            assertThat(result.getCategory()).isEqualTo(GoalCategory.HEALTH);
            assertThat(result.getTotalUsers()).isEqualTo(50);
            assertThat(result.getActiveGoals()).isEqualTo(30);
            assertThat(result.getCompletedGoals()).isEqualTo(20);
            assertThat(result.getAverageCompletionRate()).isEqualTo(40.0);
            assertThat(result.getTopStrategies()).isNotEmpty();
        }

        @Test
        @DisplayName("Should get category stats for all categories")
        void shouldGetCategoryStatsForAllCategories() {
            for (GoalCategory category : GoalCategory.values()) {
                when(goalRepository.countDistinctUsersByCategory(category)).thenReturn(10L);
                when(goalRepository.countActiveByCatgory(category)).thenReturn(5L);
                when(goalRepository.countCompletedByCategory(category)).thenReturn(3L);
                when(goalRepository.countByCategory(category)).thenReturn(10L);
                when(strugglingRequestRepository.findTopStrugglingTypesByCategory(category))
                        .thenReturn(List.of());

                var result = socialProofService.getCategoryStats(category);

                assertThat(result.getCategory()).isEqualTo(category);
                assertThat(result.getTopStrategies()).isNotEmpty();
            }
        }
    }

    @Nested
    @DisplayName("Get goal insights tests")
    class GetGoalInsightsTests {

        @Test
        @DisplayName("Should get goal insights")
        void shouldGetGoalInsights() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(goalRepository.countUsersWithSimilarGoals(GoalCategory.HEALTH)).thenReturn(100L);
            when(goalRepository.countDistinctUsersByCategory(GoalCategory.HEALTH)).thenReturn(50L);
            when(goalRepository.countActiveByCatgory(GoalCategory.HEALTH)).thenReturn(30L);
            when(goalRepository.countCompletedByCategory(GoalCategory.HEALTH)).thenReturn(20L);
            when(goalRepository.countByCategory(GoalCategory.HEALTH)).thenReturn(50L);
            when(strugglingRequestRepository.findTopStrugglingTypesByCategory(GoalCategory.HEALTH))
                    .thenReturn(List.of());

            var result = socialProofService.getGoalInsights(1L, user);

            assertThat(result.getGoalId()).isEqualTo(1L);
            assertThat(result.getCategory()).isEqualTo(GoalCategory.HEALTH);
            assertThat(result.getUsersWithSimilarGoals()).isEqualTo(100);
            assertThat(result.getSuggestedStrategies()).isNotEmpty();
            assertThat(result.getEncouragementMessage()).isNotBlank();
        }

        @Test
        @DisplayName("Should throw when goal not found")
        void shouldThrowWhenGoalNotFound() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> socialProofService.getGoalInsights(1L, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.GOAL_NOT_FOUND);
        }

        @Test
        @DisplayName("Should generate encouragement for high progress")
        void shouldGenerateEncouragementForHighProgress() {
            goal.setCurrentProgress(BigDecimal.valueOf(80));
            goal.setTargetValue(new BigDecimal("100"));

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(goalRepository.countUsersWithSimilarGoals(GoalCategory.HEALTH)).thenReturn(100L);
            when(goalRepository.countDistinctUsersByCategory(GoalCategory.HEALTH)).thenReturn(50L);
            when(goalRepository.countActiveByCatgory(GoalCategory.HEALTH)).thenReturn(30L);
            when(goalRepository.countCompletedByCategory(GoalCategory.HEALTH)).thenReturn(20L);
            when(goalRepository.countByCategory(GoalCategory.HEALTH)).thenReturn(50L);
            when(strugglingRequestRepository.findTopStrugglingTypesByCategory(GoalCategory.HEALTH))
                    .thenReturn(List.of());

            var result = socialProofService.getGoalInsights(1L, user);

            assertThat(result.getEncouragementMessage()).contains("top 25%");
        }
    }

    @Nested
    @DisplayName("Get milestone stats tests")
    class GetMilestoneStatsTests {

        @Test
        @DisplayName("Should get milestone stats")
        void shouldGetMilestoneStats() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(goalRepository.countUsersWithSimilarGoals(GoalCategory.HEALTH)).thenReturn(100L);

            var result = socialProofService.getMilestoneStats(1L, user);

            assertThat(result.getGoalId()).isEqualTo(1L);
            assertThat(result.getMilestonePercentage()).isEqualTo(50);
            assertThat(result.getMotivationalMessage()).isNotBlank();
        }

        @Test
        @DisplayName("Should handle zero target value")
        void shouldHandleZeroTargetValue() {
            goal.setTargetValue(BigDecimal.ZERO);

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(goalRepository.countUsersWithSimilarGoals(GoalCategory.HEALTH)).thenReturn(100L);

            var result = socialProofService.getMilestoneStats(1L, user);

            assertThat(result.getMilestonePercentage()).isZero();
        }

        @Test
        @DisplayName("Should handle null current progress")
        void shouldHandleNullCurrentProgress() {
            goal.setCurrentProgress(null);

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(goalRepository.countUsersWithSimilarGoals(GoalCategory.HEALTH)).thenReturn(100L);

            var result = socialProofService.getMilestoneStats(1L, user);

            assertThat(result.getMilestonePercentage()).isZero();
        }

        @Test
        @DisplayName("Should handle null target value")
        void shouldHandleNullTargetValue() {
            goal.setTargetValue(null);

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(goalRepository.countUsersWithSimilarGoals(GoalCategory.HEALTH)).thenReturn(100L);

            var result = socialProofService.getMilestoneStats(1L, user);

            assertThat(result.getMilestonePercentage()).isZero();
        }

        @Test
        @DisplayName("Should return correct milestone for various progress levels")
        void shouldReturnCorrectMilestoneForVariousProgressLevels() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(goalRepository.countUsersWithSimilarGoals(GoalCategory.HEALTH)).thenReturn(100L);

            // Test 100% milestone
            goal.setCurrentProgress(BigDecimal.valueOf(100));
            var result100 = socialProofService.getMilestoneStats(1L, user);
            assertThat(result100.getMilestonePercentage()).isEqualTo(100);

            // Test 75% milestone
            goal.setCurrentProgress(BigDecimal.valueOf(75));
            var result75 = socialProofService.getMilestoneStats(1L, user);
            assertThat(result75.getMilestonePercentage()).isEqualTo(75);

            // Test 25% milestone
            goal.setCurrentProgress(BigDecimal.valueOf(25));
            var result25 = socialProofService.getMilestoneStats(1L, user);
            assertThat(result25.getMilestonePercentage()).isEqualTo(25);

            // Test 0% milestone
            goal.setCurrentProgress(BigDecimal.valueOf(10));
            var result0 = socialProofService.getMilestoneStats(1L, user);
            assertThat(result0.getMilestonePercentage()).isZero();
        }
    }
}
