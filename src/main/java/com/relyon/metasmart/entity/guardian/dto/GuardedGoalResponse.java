package com.relyon.metasmart.entity.guardian.dto;

import com.relyon.metasmart.entity.actionplan.dto.ActionItemResponse;
import com.relyon.metasmart.entity.goal.GoalCategory;
import com.relyon.metasmart.entity.goal.GoalStatus;
import com.relyon.metasmart.entity.guardian.GuardianPermission;
import com.relyon.metasmart.entity.obstacle.dto.ObstacleEntryResponse;
import com.relyon.metasmart.entity.progress.dto.ProgressEntryResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuardedGoalResponse {

    private Long goalGuardianId;
    private Set<GuardianPermission> permissions;

    // Basic goal info (always visible)
    private Long goalId;
    private String title;
    private String description;
    private GoalCategory category;
    private GoalStatus status;
    private String ownerName;
    private LocalDate startDate;
    private LocalDate targetDate;

    // Progress info (if VIEW_PROGRESS permission)
    private BigDecimal progressPercentage;
    private BigDecimal currentProgress;
    private String targetValue;
    private String unit;
    private LocalDateTime lastProgressAt;
    private List<ProgressEntryResponse> recentProgress;

    // Streak info (if VIEW_STREAK permission)
    private Integer currentStreak;
    private Integer longestStreak;

    // Obstacles (if VIEW_OBSTACLES permission)
    private List<ObstacleEntryResponse> recentObstacles;
    private Integer unresolvedObstaclesCount;

    // Action plan (if VIEW_ACTION_PLAN permission)
    private List<ActionItemResponse> actionItems;
    private Integer completedActionsCount;
    private Integer totalActionsCount;
}
