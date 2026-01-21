package com.relyon.metasmart.entity.journal.dto;

import com.relyon.metasmart.entity.journal.Mood;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyJournalResponse {

    private Long id;
    private LocalDate journalDate;
    private String content;
    private Mood mood;
    private Boolean shieldUsed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
