package com.relyon.metasmart.entity.goal.dto;

import com.relyon.metasmart.entity.goal.GoalCategory;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be at most 255 characters")
    private String title;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;

    @NotNull(message = "Category is required")
    private GoalCategory goalCategory;

    @NotBlank(message = "Target value is required")
    private String targetValue;

    @NotBlank(message = "Unit is required")
    private String unit;

    @Size(max = 500, message = "Motivation must be at most 500 characters")
    private String motivation;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "Target date is required")
    @Future(message = "Target date must be in the future")
    private LocalDate targetDate;
}
