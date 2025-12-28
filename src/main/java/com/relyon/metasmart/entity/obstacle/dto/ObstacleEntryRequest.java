package com.relyon.metasmart.entity.obstacle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObstacleEntryRequest {

    private LocalDate entryDate;

    @NotBlank(message = "Obstacle description is required")
    @Size(max = 1000, message = "Obstacle must be at most 1000 characters")
    private String obstacle;

    @Size(max = 1000, message = "Solution must be at most 1000 characters")
    private String solution;
}
