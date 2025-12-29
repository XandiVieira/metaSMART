package com.relyon.metasmart.entity.struggling.dto;

import com.relyon.metasmart.entity.struggling.StrugglingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrugglingHelpResponse {

    private Long id;
    private Long goalId;
    private String goalTitle;
    private StrugglingType strugglingType;
    private String userMessage;
    private List<String> suggestions;
    private Boolean guardiansNotified;
    private Integer freeRequestsRemaining;
    private LocalDateTime createdAt;
}
