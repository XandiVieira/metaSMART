package com.relyon.metasmart.entity.guardian.dto;

import com.relyon.metasmart.entity.guardian.GuardianPermission;
import com.relyon.metasmart.entity.guardian.GuardianStatus;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalGuardianResponse {

    private Long id;
    private Long goalId;
    private String goalTitle;
    private Long guardianId;
    private String guardianName;
    private String guardianEmail;
    private Long ownerId;
    private String ownerName;
    private GuardianStatus status;
    private Set<GuardianPermission> permissions;
    private String inviteMessage;
    private LocalDateTime invitedAt;
    private LocalDateTime acceptedAt;
}
