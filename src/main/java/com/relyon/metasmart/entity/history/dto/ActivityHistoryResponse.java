package com.relyon.metasmart.entity.history.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityHistoryResponse {

    private LocalDate startDate;
    private LocalDate endDate;
    private int totalDays;
    private int activeDays;
    private Map<LocalDate, DailyActivityResponse> dailyActivities;
    private ActivitySummary summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivitySummary {
        private int totalTaskCompletions;
        private int totalProgressEntries;
        private int totalJournalEntries;
    }
}
