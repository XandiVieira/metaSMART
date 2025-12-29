package com.relyon.metasmart.entity.goal.dto;

import com.relyon.metasmart.entity.goal.FrequencyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalMeasurementDto {

    private String unit;
    private BigDecimal targetValue;
    private BigDecimal currentValue;
    private FrequencyDto frequency;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FrequencyDto {
        private FrequencyType type;
        private Integer value;
    }
}
