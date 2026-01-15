package com.relyon.metasmart.entity.goal.dto;

import com.relyon.metasmart.entity.goal.FrequencyType;
import com.relyon.metasmart.entity.goal.ReminderChannel;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalRemindersDto {

    private List<ReminderChannel> channels;
    private FrequencyDto frequency;
    private Boolean active;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FrequencyDto {
        private FrequencyType interval;
        private Integer customMinutes;
    }
}
