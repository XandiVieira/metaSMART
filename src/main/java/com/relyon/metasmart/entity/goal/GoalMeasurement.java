package com.relyon.metasmart.entity.goal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalMeasurement {

    @Column(name = "measurement_unit", length = 100)
    private String unit;

    @Column(name = "measurement_target_value")
    private BigDecimal targetValue;

    @Column(name = "measurement_current_value")
    private BigDecimal currentValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "measurement_frequency_type")
    private FrequencyType frequencyType;

    @Column(name = "measurement_frequency_value")
    private Integer frequencyValue;
}
