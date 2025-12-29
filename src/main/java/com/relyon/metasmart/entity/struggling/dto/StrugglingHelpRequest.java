package com.relyon.metasmart.entity.struggling.dto;

import com.relyon.metasmart.entity.struggling.StrugglingType;
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
public class StrugglingHelpRequest {

    @NotNull(message = "Struggling type is required")
    private StrugglingType strugglingType;

    @Size(max = 1000, message = "Message must be at most 1000 characters")
    private String message;

    @Builder.Default
    private Boolean notifyGuardians = false;
}
