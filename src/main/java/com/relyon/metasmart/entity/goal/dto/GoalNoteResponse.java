package com.relyon.metasmart.entity.goal.dto;

import com.relyon.metasmart.entity.goal.GoalNote;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalNoteResponse {

    private Long id;
    private Long goalId;
    private String content;
    private GoalNote.NoteType noteType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
