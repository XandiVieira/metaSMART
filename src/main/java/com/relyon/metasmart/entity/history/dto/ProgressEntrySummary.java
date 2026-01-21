package com.relyon.metasmart.entity.history.dto;

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
public class ProgressEntrySummary {

    private Long id;
    private Long goalId;
    private String goalTitle;
    private BigDecimal progressValue;
    private String unit;
    private BigDecimal percentageOfGoal;
    private String note;
    private LocalDateTime createdAt;
}
