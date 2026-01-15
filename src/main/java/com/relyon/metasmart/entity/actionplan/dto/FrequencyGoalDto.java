package com.relyon.metasmart.entity.actionplan.dto;

import com.relyon.metasmart.entity.actionplan.FrequencyPeriod;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrequencyGoalDto {

    private Integer count;
    private FrequencyPeriod period;
    private List<Integer> fixedDays;
}
