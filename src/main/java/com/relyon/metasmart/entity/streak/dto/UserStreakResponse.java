package com.relyon.metasmart.entity.streak.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStreakResponse {

    private Integer currentStreak;
    private Integer bestStreak;
    private Integer shieldsAvailable;
    private Integer shieldsUsedThisWeek;
    private Long journalEntriesThisMonth;
    private LocalDate lastActivityDate;
}
