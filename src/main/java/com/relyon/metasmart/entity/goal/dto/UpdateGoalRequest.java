package com.relyon.metasmart.entity.goal.dto;

import com.relyon.metasmart.entity.goal.GoalCategory;
import com.relyon.metasmart.entity.goal.GoalStatus;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGoalRequest {

    @Size(max = 255, message = "Title must be at most 255 characters")
    private String title;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;

    private GoalCategory goalCategory;

    private BigDecimal targetValue;

    private String unit;

    private BigDecimal currentProgress;

    @Size(max = 500, message = "Motivation must be at most 500 characters")
    private String motivation;

    private LocalDate startDate;

    private LocalDate targetDate;

    private GoalStatus goalStatus;
}
