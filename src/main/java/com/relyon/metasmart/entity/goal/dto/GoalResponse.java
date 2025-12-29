package com.relyon.metasmart.entity.goal.dto;

import com.relyon.metasmart.entity.goal.GoalCategory;
import com.relyon.metasmart.entity.goal.GoalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalResponse {

    private Long id;
    private String title;
    private String description;

    private GoalCategoryDto category;

    @Deprecated
    private GoalCategory goalCategory;

    private String targetValue;
    private String unit;
    private BigDecimal currentProgress;
    private BigDecimal progressPercentage;
    private String motivation;
    private LocalDate startDate;
    private LocalDate targetDate;

    @Deprecated
    private GoalStatus goalStatus;

    private GoalStatusDto status;

    private GoalPillarsDto pillars;

    private GoalMeasurementDto measurement;

    private GoalRemindersDto reminders;

    private EmotionalAnchorsDto emotionalAnchors;

    private AiSupportDto aiSupport;

    private List<CheckinDto> checkins;

    private SupportSystemDto supportSystem;

    private List<String> tags;

    private ActionPlanDto actionPlan;

    private SmartPillarsDto smartPillars;
    private Integer setupCompletionPercentage;
    private Integer currentStreak;
    private Integer longestStreak;

    private List<MilestoneDto> milestones;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
