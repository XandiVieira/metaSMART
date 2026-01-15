package com.relyon.metasmart.service;

import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.goal.GoalCategory;
import com.relyon.metasmart.entity.social.dto.CategoryStatsResponse;
import com.relyon.metasmart.entity.social.dto.GlobalStatsResponse;
import com.relyon.metasmart.entity.social.dto.GoalInsightsResponse;
import com.relyon.metasmart.entity.social.dto.MilestoneStatsResponse;
import com.relyon.metasmart.entity.struggling.StrugglingType;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.repository.GoalRepository;
import com.relyon.metasmart.repository.ProgressEntryRepository;
import com.relyon.metasmart.repository.StrugglingRequestRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialProofService {

    private final GoalRepository goalRepository;
    private final ProgressEntryRepository progressEntryRepository;
    private final StrugglingRequestRepository strugglingRequestRepository;

    @Setter(onMethod_ = {@Autowired, @Lazy})
    private SocialProofService self;

    @Transactional(readOnly = true)
    public GlobalStatsResponse getGlobalStats() {
        log.debug("Fetching global social proof stats");

        var totalUsers = goalRepository.countDistinctUsers();
        var totalGoals = goalRepository.count();
        var completedGoals = goalRepository.countCompletedGoals();
        var totalProgress = progressEntryRepository.countAllProgressEntries();
        // Streaks are calculated dynamically per goal, not stored
        var averageStreak = 0;
        var longestStreak = 0;

        var completionRate = totalGoals > 0
                ? BigDecimal.valueOf(completedGoals)
                .divide(BigDecimal.valueOf(totalGoals), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue()
                : 0.0;

        Map<String, Long> goalsByCategory = new LinkedHashMap<>();
        Map<String, Double> completionByCategory = new LinkedHashMap<>();

        for (GoalCategory category : GoalCategory.values()) {
            var categoryCount = goalRepository.countByCategory(category);
            var categoryCompleted = goalRepository.countCompletedByCategory(category);

            goalsByCategory.put(category.name(), categoryCount);

            var categoryRate = categoryCount > 0
                    ? BigDecimal.valueOf(categoryCompleted)
                    .divide(BigDecimal.valueOf(categoryCount), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue()
                    : 0.0;
            completionByCategory.put(category.name(), categoryRate);
        }

        return GlobalStatsResponse.builder()
                .totalActiveUsers(totalUsers)
                .totalGoalsCreated(totalGoals)
                .totalGoalsCompleted(completedGoals)
                .overallCompletionRate(completionRate)
                .totalProgressEntries(totalProgress)
                .goalsByCategory(goalsByCategory)
                .completionRateByCategory(completionByCategory)
                .averageStreakAcrossUsers(averageStreak)
                .longestStreakEver(longestStreak)
                .build();
    }

    @Transactional(readOnly = true)
    public CategoryStatsResponse getCategoryStats(GoalCategory category) {
        log.debug("Fetching social proof stats for category: {}", category);

        var totalUsers = goalRepository.countDistinctUsersByCategory(category);
        var activeGoals = goalRepository.countActiveByCatgory(category);
        var completedGoals = goalRepository.countCompletedByCategory(category);
        var totalGoals = goalRepository.countByCategory(category);

        var completionRate = totalGoals > 0
                ? BigDecimal.valueOf(completedGoals)
                .divide(BigDecimal.valueOf(totalGoals), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue()
                : 0.0;

        // Streaks are calculated dynamically per goal, not stored
        var averageStreak = 0;
        var longestStreak = 0;

        var commonObstacles = getCommonObstaclesByCategory(category);
        var topStrategies = getStrategiesForCategory(category);

        return CategoryStatsResponse.builder()
                .category(category)
                .totalUsers(totalUsers)
                .activeGoals(activeGoals)
                .completedGoals(completedGoals)
                .averageCompletionRate(completionRate)
                .averageDaysToComplete(estimateAverageDays(category))
                .averageStreak(averageStreak)
                .longestStreak(longestStreak)
                .commonObstacles(commonObstacles)
                .topStrategies(topStrategies)
                .build();
    }

    @Transactional(readOnly = true)
    public GoalInsightsResponse getGoalInsights(Long goalId, User user) {
        log.debug("Fetching insights for goal: {} by user: {}", goalId, user.getId());

        var goal = goalRepository.findByIdAndOwner(goalId, user)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND));

        var category = goal.getGoalCategory();
        var similarUsers = goalRepository.countUsersWithSimilarGoals(category);
        var categoryStats = self.getCategoryStats(category);
        var commonObstacles = getCommonObstaclesByCategory(category);
        var strategies = getStrategiesForCategory(category);

        var encouragement = generateEncouragementMessage(goal, categoryStats);

        return GoalInsightsResponse.builder()
                .goalId(goalId)
                .category(category)
                .usersWithSimilarGoals(similarUsers)
                .similarGoalsCompletionRate(categoryStats.getAverageCompletionRate())
                .averageDaysToComplete(estimateAverageDays(category))
                .commonObstacles(commonObstacles)
                .suggestedStrategies(strategies)
                .encouragementMessage(encouragement)
                .build();
    }

    @Transactional(readOnly = true)
    public MilestoneStatsResponse getMilestoneStats(Long goalId, User user) {
        log.debug("Fetching milestone stats for goal: {}", goalId);

        var goal = goalRepository.findByIdAndOwner(goalId, user)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND));

        var progressPercentage = calculateProgressPercentage(goal);
        var currentMilestone = getMilestoneForPercentage(progressPercentage);
        var category = goal.getGoalCategory();

        var similarUsers = goalRepository.countUsersWithSimilarGoals(category);
        var estimatedAtMilestone = estimateUsersAtMilestone(similarUsers, currentMilestone);
        var percentageAtPoint = similarUsers > 0
                ? BigDecimal.valueOf(estimatedAtMilestone)
                .divide(BigDecimal.valueOf(similarUsers), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue()
                : 0.0;

        var motivationalMessage = generateMilestoneMessage(currentMilestone, percentageAtPoint);

        return MilestoneStatsResponse.builder()
                .goalId(goalId)
                .milestonePercentage(currentMilestone)
                .usersReachedThisMilestone(estimatedAtMilestone)
                .percentageOfUsersAtThisPoint(percentageAtPoint)
                .averageDaysToReach(estimateDaysToMilestone(currentMilestone))
                .motivationalMessage(motivationalMessage)
                .build();
    }

    private List<StrugglingType> getCommonObstaclesByCategory(GoalCategory category) {
        var results = strugglingRequestRepository.findTopStrugglingTypesByCategory(category);
        return results.stream()
                .limit(3)
                .map(row -> (StrugglingType) row[0])
                .toList();
    }

    private List<String> getStrategiesForCategory(GoalCategory category) {
        return switch (category) {
            case HEALTH -> List.of(
                    "Start with 5-minute daily sessions",
                    "Track progress visually with photos",
                    "Find an accountability partner"
            );
            case FINANCE -> List.of(
                    "Automate savings transfers",
                    "Review spending weekly",
                    "Set up milestone rewards"
            );
            case EDUCATION -> List.of(
                    "Use spaced repetition for learning",
                    "Set specific study times",
                    "Break content into small chunks"
            );
            case CAREER -> List.of(
                    "Network with 1 person weekly",
                    "Document achievements as they happen",
                    "Set quarterly review checkpoints"
            );
            case RELATIONSHIPS -> List.of(
                    "Schedule dedicated time weekly",
                    "Practice active listening",
                    "Express gratitude daily"
            );
            case PERSONAL_DEVELOPMENT -> List.of(
                    "Journal progress and reflections",
                    "Start with one small habit",
                    "Celebrate small wins"
            );
            case HOBBIES -> List.of(
                    "Dedicate consistent time slots",
                    "Join communities of similar interest",
                    "Share your progress publicly"
            );
            case OTHER -> List.of(
                    "Break goal into smaller milestones",
                    "Track progress consistently",
                    "Review and adjust weekly"
            );
        };
    }

    private int calculateProgressPercentage(Goal goal) {
        if (goal.getTargetValue() == null || goal.getCurrentProgress() == null) {
            return 0;
        }
        var target = new BigDecimal(goal.getTargetValue());
        if (target.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        return goal.getCurrentProgress()
                .divide(target, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .min(BigDecimal.valueOf(100))
                .intValue();
    }

    private int getMilestoneForPercentage(int percentage) {
        if (percentage >= 100) return 100;
        if (percentage >= 75) return 75;
        if (percentage >= 50) return 50;
        if (percentage >= 25) return 25;
        return 0;
    }

    private long estimateUsersAtMilestone(long totalUsers, int milestone) {
        // Estimated distribution based on typical completion curves
        return switch (milestone) {
            case 0 -> totalUsers;
            case 25 -> (long) (totalUsers * 0.70);
            case 50 -> (long) (totalUsers * 0.45);
            case 75 -> (long) (totalUsers * 0.25);
            case 100 -> (long) (totalUsers * 0.15);
            default -> totalUsers;
        };
    }

    private int estimateDaysToMilestone(int milestone) {
        return switch (milestone) {
            case 25 -> 7;
            case 50 -> 21;
            case 75 -> 45;
            case 100 -> 90;
            default -> 0;
        };
    }

    private int estimateAverageDays(GoalCategory category) {
        return switch (category) {
            case HEALTH -> 60;
            case FINANCE -> 90;
            case EDUCATION -> 45;
            case CAREER -> 120;
            case RELATIONSHIPS -> 30;
            case PERSONAL_DEVELOPMENT -> 66;
            case HOBBIES -> 30;
            case OTHER -> 60;
        };
    }

    private String generateEncouragementMessage(Goal goal, CategoryStatsResponse stats) {
        var progress = calculateProgressPercentage(goal);
        var completionRate = stats.getAverageCompletionRate();

        if (progress >= 75) {
            return String.format("You're in the top 25%% of users! %.0f%% of people with similar goals reach completion.", completionRate);
        } else if (progress >= 50) {
            return String.format("Great progress! You've passed the halfway point. %d users are working on similar goals.", stats.getTotalUsers());
        } else if (progress >= 25) {
            return String.format("Keep going! Most users see momentum build after the 25%% mark. Average streak in this category: %d days.", stats.getAverageStreak());
        } else {
            return String.format("You've joined %d others on this journey. Consistency is key - aim for small daily progress!", stats.getTotalUsers());
        }
    }

    private String generateMilestoneMessage(int milestone, double percentageAtPoint) {
        return switch (milestone) {
            case 100 ->
                    String.format("Congratulations! You're among the %.0f%% who completed their goal!", percentageAtPoint);
            case 75 ->
                    String.format("Amazing! Only %.0f%% of users reach this point. The finish line is in sight!", percentageAtPoint);
            case 50 ->
                    String.format("Halfway there! %.0f%% of users with similar goals have reached this milestone.", percentageAtPoint);
            case 25 ->
                    String.format("Great start! %.0f%% of users hit this first milestone. Keep the momentum!", percentageAtPoint);
            default -> "Every journey starts with a single step. You've got this!";
        };
    }
}
