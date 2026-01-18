package com.relyon.metasmart.entity.template.dto;

import com.relyon.metasmart.entity.goal.GoalCategory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalTemplateResponse {

    private Long id;
    private String name;
    private String description;
    private String defaultTitle;
    private String defaultDescription;
    private GoalCategory defaultCategory;
    private BigDecimal defaultTargetValue;
    private String defaultUnit;
    private String defaultMotivation;
    private Integer defaultDurationDays;
    private Boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
