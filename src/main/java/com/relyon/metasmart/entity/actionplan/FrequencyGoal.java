package com.relyon.metasmart.entity.actionplan;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrequencyGoal {

    @Column(name = "frequency_count")
    private Integer count;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency_period")
    private FrequencyPeriod period;

    @Column(name = "frequency_fixed_days")
    private String fixedDays;
}
