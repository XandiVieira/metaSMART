package com.relyon.metasmart.entity.guardian.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactToNudgeRequest {

    @NotBlank(message = "Reaction is required")
    @Size(max = 10, message = "Reaction must be at most 10 characters")
    private String reaction;
}
