package com.relyon.metasmart.entity.progress.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressEntryResponse {

    private Long id;
    private Long goalId;
    private BigDecimal progressValue;
    private String note;
    private LocalDateTime createdAt;
}
