package com.relyon.metasmart.entity.history.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyActivityResponse {

    private LocalDate date;
    private List<TaskCompletionSummary> taskCompletions;
    private List<ProgressEntrySummary> progressEntries;
    private JournalEntrySummary journalEntry;
    private boolean hasActivity;
}
