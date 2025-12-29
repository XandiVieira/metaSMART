package com.relyon.metasmart.entity.actionplan.dto;

import com.relyon.metasmart.entity.actionplan.TaskPriority;
import com.relyon.metasmart.entity.actionplan.TaskType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionItemResponse {

    private Long id;
    private String title;
    private String description;
    private TaskType taskType;
    private LocalDate targetDate;
    private Boolean completed;
    private LocalDateTime completedAt;
    private TaskPriority priority;
    private Integer orderIndex;
    private Integer impactScore;
    private Integer effortEstimate;
    private List<String> context;
    private List<Long> dependencies;
    private TaskRecurrenceDto recurrence;
    private FrequencyGoalDto frequencyGoal;
    private ReminderOverrideDto remindersOverride;
    private List<TaskCompletionDto> completionHistory;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Deprecated
    public LocalDate getDueDate() {
        return targetDate;
    }
}
