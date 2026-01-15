package com.relyon.metasmart.entity.goal.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneDto {

    private BigDecimal value;
    private String label;
    private Boolean achieved;
}
