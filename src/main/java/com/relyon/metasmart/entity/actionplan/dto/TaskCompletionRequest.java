package com.relyon.metasmart.entity.actionplan.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCompletionRequest {

    @NotNull(message = "Date is required")
    private LocalDate date;

    @Size(max = 500, message = "Note must be at most 500 characters")
    private String note;
}
