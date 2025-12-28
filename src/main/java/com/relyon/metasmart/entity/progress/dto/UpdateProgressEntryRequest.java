package com.relyon.metasmart.entity.progress.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProgressEntryRequest {

    @Positive(message = "Value must be positive")
    private BigDecimal progressValue;

    @Size(max = 500, message = "Note must be at most 500 characters")
    private String note;
}
