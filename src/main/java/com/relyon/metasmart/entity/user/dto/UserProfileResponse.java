package com.relyon.metasmart.entity.user.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String name;
    private String email;
    private String profilePictureUrl;
    private LocalDateTime joinedAt;
    private long totalGoals;
    private long completedGoals;
    private int streakShields;
}
