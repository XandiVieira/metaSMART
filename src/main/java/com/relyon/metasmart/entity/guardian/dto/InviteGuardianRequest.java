package com.relyon.metasmart.entity.guardian.dto;

import com.relyon.metasmart.entity.guardian.GuardianPermission;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteGuardianRequest {

    @NotBlank(message = "Guardian email is required")
    @Email(message = "Invalid email format")
    private String guardianEmail;

    @NotEmpty(message = "At least one permission is required")
    private Set<GuardianPermission> permissions;

    @Size(max = 500, message = "Invite message must be at most 500 characters")
    private String inviteMessage;
}
