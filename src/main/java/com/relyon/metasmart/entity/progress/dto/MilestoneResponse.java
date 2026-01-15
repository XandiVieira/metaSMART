package com.relyon.metasmart.entity.progress.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneResponse {

    private Long id;
    private Long goalId;
    private Integer percentage;
    private String description;
    private Boolean achieved;
    private LocalDateTime achievedAt;
}
