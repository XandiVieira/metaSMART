package com.relyon.metasmart.entity.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String name;
    private String email;
    private LocalDateTime joinedAt;
    private long totalGoals;
    private long completedGoals;
    private int streakShields;
}
