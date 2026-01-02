package com.relyon.metasmart.entity.progress.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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

        @NotNull(message = "Date is required")
        private LocalDate date;

        @NotNull(message = "Progress value is required")
        @Positive(message = "Progress value must be positive")
        private BigDecimal progressValue;

        @Size(max = 500, message = "Note must be at most 500 characters")
        private String note;
    }
}
