package com.relyon.metasmart.entity.streak.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreakSummaryResponse {

    private StreakResponse userStreak;
    private List<StreakResponse> goalStreaks;
    private List<StreakResponse> taskStreaks;
}
