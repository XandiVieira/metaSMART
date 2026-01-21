package com.relyon.metasmart.entity.journal.dto;

import com.relyon.metasmart.entity.journal.Mood;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyJournalRequest {

    @NotNull(message = "Journal date is required")
    private LocalDate journalDate;

    @Size(max = 2000, message = "Content must not exceed 2000 characters")
    private String content;

    private Mood mood;
}
