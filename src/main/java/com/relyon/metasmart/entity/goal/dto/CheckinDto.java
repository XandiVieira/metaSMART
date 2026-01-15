package com.relyon.metasmart.entity.goal.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckinDto {

    private Long id;
    private LocalDateTime createdAt;
    private String note;
    private BigDecimal progressDelta;
}
