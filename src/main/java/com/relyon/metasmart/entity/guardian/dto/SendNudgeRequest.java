package com.relyon.metasmart.entity.guardian.dto;

import com.relyon.metasmart.entity.guardian.NudgeType;
import jakarta.validation.constraints.NotBlank;
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
public class SendNudgeRequest {

    @NotBlank(message = "Message is required")
    @Size(max = 500, message = "Message must be at most 500 characters")
    private String message;

    @NotNull(message = "Nudge type is required")
    private NudgeType nudgeType;
}
