package com.relyon.metasmart.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.relyon.metasmart.config.CorsConfig;
import com.relyon.metasmart.config.JwtService;
import com.relyon.metasmart.config.RateLimitConfig;
import com.relyon.metasmart.config.SecurityConfig;
import com.relyon.metasmart.entity.goal.GoalCategory;
import com.relyon.metasmart.entity.social.dto.CategoryStatsResponse;
import com.relyon.metasmart.entity.social.dto.GlobalStatsResponse;
import com.relyon.metasmart.entity.social.dto.GoalInsightsResponse;
import com.relyon.metasmart.entity.social.dto.MilestoneStatsResponse;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.GlobalExceptionHandler;
import com.relyon.metasmart.service.SocialProofService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SocialProofController.class)
@Import({SecurityConfig.class, CorsConfig.class, RateLimitConfig.class, GlobalExceptionHandler.class})
class SocialProofControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SocialProofService socialProofService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private User user;
    private GlobalStatsResponse globalStatsResponse;
    private CategoryStatsResponse categoryStatsResponse;
    private GoalInsightsResponse goalInsightsResponse;
    private MilestoneStatsResponse milestoneStatsResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("John")
                .email("john@test.com")
                .password("password")
                .build();

        globalStatsResponse = GlobalStatsResponse.builder()
                .totalActiveUsers(1000L)
                .totalGoalsCreated(5000L)
                .totalGoalsCompleted(2500L)
                .overallCompletionRate(50.0)
                .totalProgressEntries(25000L)
                .goalsByCategory(Map.of("HEALTH", 1000L, "FINANCE", 800L))
                .completionRateByCategory(Map.of("HEALTH", 55.0, "FINANCE", 45.0))
                .averageStreakAcrossUsers(7)
                .longestStreakEver(365)
                .build();

        categoryStatsResponse = CategoryStatsResponse.builder()
                .category(GoalCategory.HEALTH)
                .totalUsers(500L)
                .activeGoals(300L)
                .completedGoals(200L)
                .averageCompletionRate(66.7)
                .averageDaysToComplete(45.0)
                .averageStreak(10)
                .longestStreak(180)
                .commonObstacles(List.of())
                .topStrategies(List.of("Consistency", "Daily tracking"))
                .build();

        goalInsightsResponse = GoalInsightsResponse.builder()
                .goalId(1L)
                .category(GoalCategory.HEALTH)
                .usersWithSimilarGoals(150L)
                .similarGoalsCompletionRate(72.5)
                .averageDaysToComplete(30)
                .commonObstacles(List.of())
                .suggestedStrategies(List.of("Start small", "Track progress"))
                .encouragementMessage("You're on the right track!")
                .build();

        milestoneStatsResponse = MilestoneStatsResponse.builder()
                .goalId(1L)
                .milestonePercentage(50)
                .usersReachedThisMilestone(100L)
                .percentageOfUsersAtThisPoint(65.0)
                .averageDaysToReach(15)
                .motivationalMessage("Halfway there!")
                .build();
    }

    @Nested
    @DisplayName("Global stats tests")
    class GlobalStatsTests {

        @Test
        @DisplayName("Should get global stats")
        void shouldGetGlobalStats() throws Exception {
            when(socialProofService.getGlobalStats()).thenReturn(globalStatsResponse);

            mockMvc.perform(get("/api/v1/social/stats")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalActiveUsers").value(1000L))
                    .andExpect(jsonPath("$.totalGoalsCreated").value(5000L))
                    .andExpect(jsonPath("$.totalGoalsCompleted").value(2500L))
                    .andExpect(jsonPath("$.overallCompletionRate").value(50.0))
                    .andExpect(jsonPath("$.averageStreakAcrossUsers").value(7))
                    .andExpect(jsonPath("$.longestStreakEver").value(365));
        }
    }

    @Nested
    @DisplayName("Category stats tests")
    class CategoryStatsTests {

        @Test
        @DisplayName("Should get category stats")
        void shouldGetCategoryStats() throws Exception {
            when(socialProofService.getCategoryStats(GoalCategory.HEALTH)).thenReturn(categoryStatsResponse);

            mockMvc.perform(get("/api/v1/social/stats/category/HEALTH")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.category").value("HEALTH"))
                    .andExpect(jsonPath("$.totalUsers").value(500L))
                    .andExpect(jsonPath("$.activeGoals").value(300L))
                    .andExpect(jsonPath("$.completedGoals").value(200L))
                    .andExpect(jsonPath("$.averageCompletionRate").value(66.7))
                    .andExpect(jsonPath("$.averageStreak").value(10));
        }
    }

    @Nested
    @DisplayName("Goal insights tests")
    class GoalInsightsTests {

        @Test
        @DisplayName("Should get goal insights")
        void shouldGetGoalInsights() throws Exception {
            when(socialProofService.getGoalInsights(eq(1L), any(User.class))).thenReturn(goalInsightsResponse);

            mockMvc.perform(get("/api/v1/social/goals/1/insights")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.goalId").value(1L))
                    .andExpect(jsonPath("$.category").value("HEALTH"))
                    .andExpect(jsonPath("$.usersWithSimilarGoals").value(150L))
                    .andExpect(jsonPath("$.similarGoalsCompletionRate").value(72.5))
                    .andExpect(jsonPath("$.averageDaysToComplete").value(30))
                    .andExpect(jsonPath("$.encouragementMessage").value("You're on the right track!"));
        }
    }

    @Nested
    @DisplayName("Milestone stats tests")
    class MilestoneStatsTests {

        @Test
        @DisplayName("Should get milestone stats")
        void shouldGetMilestoneStats() throws Exception {
            when(socialProofService.getMilestoneStats(eq(1L), any(User.class))).thenReturn(milestoneStatsResponse);

            mockMvc.perform(get("/api/v1/social/goals/1/milestone-stats")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.goalId").value(1L))
                    .andExpect(jsonPath("$.milestonePercentage").value(50))
                    .andExpect(jsonPath("$.usersReachedThisMilestone").value(100L))
                    .andExpect(jsonPath("$.percentageOfUsersAtThisPoint").value(65.0))
                    .andExpect(jsonPath("$.averageDaysToReach").value(15))
                    .andExpect(jsonPath("$.motivationalMessage").value("Halfway there!"));
        }
    }
}