package com.relyon.metasmart.entity.streak.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreakResponse {

    private Long id;
    private Long goalId;
    private Long actionItemId;
    private Integer currentMaintainedStreak;
    private Integer bestMaintainedStreak;
    private Integer currentPerfectStreak;
    private Integer bestPerfectStreak;
    private LocalDateTime lastUpdatedAt;
    private String level;
}
