package com.relyon.metasmart.entity.actionplan.dto;

import com.relyon.metasmart.entity.actionplan.TaskPriority;
import com.relyon.metasmart.entity.actionplan.TaskType;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateActionItemRequest {

    @Size(max = 255, message = "Title must be at most 255 characters")
    private String title;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    private TaskType taskType;

    private LocalDate targetDate;

    @Deprecated
    private LocalDate dueDate;

    private TaskPriority priority;

    private Integer orderIndex;

    private Boolean completed;

    private Integer impactScore;

    private Integer effortEstimate;

    private List<String> context;

    private List<Long> dependencies;

    private TaskRecurrenceDto recurrence;

    private FrequencyGoalDto frequencyGoal;

    private ReminderOverrideDto remindersOverride;

    @Size(max = 1000, message = "Notes must be at most 1000 characters")
    private String notes;
}
