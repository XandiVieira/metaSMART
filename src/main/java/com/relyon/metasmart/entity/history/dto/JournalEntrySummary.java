package com.relyon.metasmart.entity.history.dto;

import com.relyon.metasmart.entity.journal.Mood;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntrySummary {

    private Long id;
    private String content;
    private Mood mood;
    private Boolean shieldUsed;
    private LocalDateTime createdAt;
}
