package com.relyon.metasmart.entity.template.dto;

import com.relyon.metasmart.entity.goal.GoalCategory;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGoalTemplateRequest {

    @Size(max = 255, message = "Template name must be at most 255 characters")
    private String name;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    @Size(max = 255, message = "Default title must be at most 255 characters")
    private String defaultTitle;

    @Size(max = 1000, message = "Default description must be at most 1000 characters")
    private String defaultDescription;

    private GoalCategory defaultCategory;

    @Positive(message = "Default target value must be positive")
    private BigDecimal defaultTargetValue;

    @Size(max = 50, message = "Default unit must be at most 50 characters")
    private String defaultUnit;

    @Size(max = 500, message = "Default motivation must be at most 500 characters")
    private String defaultMotivation;

    @Positive(message = "Default duration must be positive")
    private Integer defaultDurationDays;

    private Boolean isPublic;
}
