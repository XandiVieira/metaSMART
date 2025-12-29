package com.relyon.metasmart.service;

import com.relyon.metasmart.entity.dashboard.dto.DashboardResponse;
import com.relyon.metasmart.entity.dashboard.dto.GoalStatsResponse;
import com.relyon.metasmart.entity.dashboard.dto.StreakAtRiskDto;
import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.goal.GoalStatus;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.repository.GoalRepository;
import com.relyon.metasmart.repository.ProgressEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final GoalRepository goalRepository;
    private final ProgressEntryRepository progressEntryRepository;
    private final ReflectionService reflectionService;
    private final GuardianNudgeService guardianNudgeService;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(User user) {
        log.debug("Getting dashboard for user: {}", user.getEmail());

        var activeGoals = goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.ACTIVE);
        var completedGoals = goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.COMPLETED);
        var pendingReflections = reflectionService.getPendingReflections(user).size();
        var unreadNudges = guardianNudgeService.countUnreadNudges(user);
        var streaksAtRisk = findStreaksAtRisk(user);

        return DashboardResponse.builder()
                .activeGoalsCount(activeGoals)
                .completedGoalsCount(completedGoals)
                .pendingReflectionsCount(pendingReflections)
                .unreadNudgesCount(unreadNudges)
                .streakShieldsAvailable(user.getStreakShields())
                .streaksAtRisk(streaksAtRisk)
                .build();
    }

    @Transactional(readOnly = true)
    public GoalStatsResponse getGoalStats(User user) {
        log.debug("Getting goal stats for user: {}", user.getEmail());

        var totalGoals = goalRepository.countByOwnerAndArchivedAtIsNull(user);
        var activeGoals = goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.ACTIVE);
        var completedGoals = goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.COMPLETED);
        var pausedGoals = goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.PAUSED);
        var abandonedGoals = goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.ABANDONED);

        var completionRate = totalGoals > 0
            ? BigDecimal.valueOf(completedGoals * 100.0 / totalGoals).setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        var streaks = calculateBestStreaks(user);
        var goalsByCategory = calculateGoalsByCategory(user);

        return GoalStatsResponse.builder()
                .totalGoals(totalGoals)
                .activeGoals(activeGoals)
                .completedGoals(completedGoals)
                .pausedGoals(pausedGoals)
                .abandonedGoals(abandonedGoals)
                .completionRate(completionRate)
                .bestStreak(streaks[0])
                .currentBestStreak(streaks[1])
                .goalsByCategory(goalsByCategory)
                .build();
    }

    private List<StreakAtRiskDto> findStreaksAtRisk(User user) {
        var activeGoals = goalRepository.findByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.ACTIVE);
        var streaksAtRisk = new ArrayList<StreakAtRiskDto>();

        for (var goal : activeGoals) {
            var streakInfo = calculateStreakInfo(goal);
            var currentStreak = streakInfo[0];
            var daysWithoutProgress = streakInfo[1];

            // Only include goals with streak > 0 and at risk (no progress today)
            if (currentStreak > 0 && daysWithoutProgress >= 1) {
                streaksAtRisk.add(StreakAtRiskDto.builder()
                        .goalId(goal.getId())
                        .goalTitle(goal.getTitle())
                        .currentStreak(currentStreak)
                        .daysWithoutProgress(daysWithoutProgress)
                        .build());
            }
        }

        return streaksAtRisk;
    }

    private int[] calculateStreakInfo(Goal goal) {
        var dates = progressEntryRepository.findDistinctProgressDates(goal);
        if (dates.isEmpty()) {
            return new int[]{0, Integer.MAX_VALUE};
        }

        var today = LocalDate.now();
        var lastProgressDate = dates.getFirst();
        var daysWithoutProgress = (int) ChronoUnit.DAYS.between(lastProgressDate, today);

        var currentStreak = 0;
        var streak = 1;

        if (lastProgressDate.equals(today) || lastProgressDate.equals(today.minusDays(1))) {
            currentStreak = 1;
        }

        for (var dateIndex = 0; dateIndex < dates.size() - 1; dateIndex++) {
            var current = dates.get(dateIndex);
            var next = dates.get(dateIndex + 1);

            if (current.minusDays(1).equals(next)) {
                streak++;
                if (lastProgressDate.equals(today) || lastProgressDate.equals(today.minusDays(1))) {
                    currentStreak = streak;
                }
            } else {
                break;
            }
        }

        return new int[]{currentStreak, daysWithoutProgress};
    }

    private int[] calculateBestStreaks(User user) {
        var allGoals = goalRepository.findByOwnerAndArchivedAtIsNull(user, org.springframework.data.domain.Pageable.unpaged());
        var bestStreak = 0;
        var currentBestStreak = 0;

        for (var goal : allGoals) {
            var streaks = calculateStreaks(goal);
            bestStreak = Math.max(bestStreak, streaks[1]);
            if (goal.getGoalStatus() == GoalStatus.ACTIVE) {
                currentBestStreak = Math.max(currentBestStreak, streaks[0]);
            }
        }

        return new int[]{bestStreak, currentBestStreak};
    }

    private int[] calculateStreaks(Goal goal) {
        var dates = progressEntryRepository.findDistinctProgressDates(goal);
        if (dates.isEmpty()) {
            return new int[]{0, 0};
        }

        var currentStreak = 0;
        var longestStreak = 0;
        var streak = 1;
        var today = LocalDate.now();

        if (dates.getFirst().equals(today) || dates.getFirst().equals(today.minusDays(1))) {
            currentStreak = 1;
        }

        for (var dateIndex = 0; dateIndex < dates.size() - 1; dateIndex++) {
            var current = dates.get(dateIndex);
            var next = dates.get(dateIndex + 1);

            if (current.minusDays(1).equals(next)) {
                streak++;
                if (dateIndex == 0 || dates.getFirst().equals(today) || dates.getFirst().equals(today.minusDays(1))) {
                    currentStreak = streak;
                }
            } else {
                longestStreak = Math.max(longestStreak, streak);
                streak = 1;
            }
        }
        longestStreak = Math.max(longestStreak, streak);

        return new int[]{currentStreak, longestStreak};
    }

    private Map<String, Long> calculateGoalsByCategory(User user) {
        var categoryMap = new HashMap<String, Long>();
        var allGoals = goalRepository.findByOwnerAndArchivedAtIsNull(user, org.springframework.data.domain.Pageable.unpaged());

        for (var goal : allGoals) {
            var category = goal.getGoalCategory().name();
            categoryMap.merge(category, 1L, Long::sum);
        }

        return categoryMap;
    }
}
