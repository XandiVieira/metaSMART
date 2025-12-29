package com.relyon.metasmart.entity.actionplan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReminderOverrideDto {

    private Boolean enabled;
    private FrequencyDto frequency;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FrequencyDto {
        private String interval;
        private Integer customMinutes;
    }
}
