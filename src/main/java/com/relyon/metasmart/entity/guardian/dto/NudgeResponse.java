package com.relyon.metasmart.entity.guardian.dto;

import com.relyon.metasmart.entity.guardian.NudgeType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NudgeResponse {

    private Long id;
    private Long goalGuardianId;
    private String guardianName;
    private String goalTitle;
    private String message;
    private NudgeType nudgeType;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private String reaction;
    private boolean isRead;
}
