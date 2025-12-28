package com.relyon.metasmart.entity.progress.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneRequest {

    @NotNull(message = "Percentage is required")
    @Min(value = 1, message = "Percentage must be at least 1")
    @Max(value = 100, message = "Percentage must be at most 100")
    private Integer percentage;

    @Size(max = 255, message = "Description must be at most 255 characters")
    private String description;
}
