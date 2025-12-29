package com.relyon.metasmart.entity.struggling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrugglingStatusResponse {

    private Integer freeRequestsUsedThisMonth;
    private Integer freeRequestsRemaining;
    private Integer paidRequestsAvailable;
    private Boolean canRequestHelp;
}
