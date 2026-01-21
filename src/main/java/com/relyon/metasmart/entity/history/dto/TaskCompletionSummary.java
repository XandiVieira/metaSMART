package com.relyon.metasmart.entity.history.dto;

import com.relyon.metasmart.entity.actionplan.CompletionStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCompletionSummary {

    private Long id;
    private Long actionItemId;
    private String actionItemTitle;
    private Long goalId;
    private String goalTitle;
    private CompletionStatus status;
    private String note;
    private LocalDateTime completedAt;
}
