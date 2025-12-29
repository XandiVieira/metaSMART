package com.relyon.metasmart.entity.progress.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkProgressRequest {

    @NotEmpty(message = "At least one progress entry is required")
    @Valid
    private List<ProgressItem> entries;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressItem {
        private LocalDate date;
        private BigDecimal progressValue;
        private String note;
    }
}
