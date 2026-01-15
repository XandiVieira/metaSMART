package com.relyon.metasmart.entity.obstacle.dto;

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
public class ObstacleEntryResponse {

    private Long id;
    private LocalDate entryDate;
    private String obstacle;
    private String solution;
    private Boolean resolved;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
