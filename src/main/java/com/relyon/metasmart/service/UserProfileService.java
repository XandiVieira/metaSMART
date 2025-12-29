package com.relyon.metasmart.service;

import com.relyon.metasmart.entity.goal.GoalStatus;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.entity.user.dto.UpdateProfileRequest;
import com.relyon.metasmart.entity.user.dto.UserProfileResponse;
import com.relyon.metasmart.repository.GoalRepository;
import com.relyon.metasmart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final GoalRepository goalRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(User user) {
        log.debug("Getting profile for user: {}", user.getEmail());

        var totalGoals = goalRepository.countByOwnerAndArchivedAtIsNull(user);
        var completedGoals = goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.COMPLETED);

        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .joinedAt(user.getCreatedAt())
                .totalGoals(totalGoals)
                .completedGoals(completedGoals)
                .streakShields(user.getStreakShields())
                .build();
    }

    @Transactional
    public UserProfileResponse updateProfile(User user, UpdateProfileRequest request) {
        log.debug("Updating profile for user: {}", user.getEmail());

        Optional.ofNullable(request.getName()).ifPresent(user::setName);

        var savedUser = userRepository.save(user);
        log.info("Profile updated for user: {}", savedUser.getEmail());

        return getProfile(savedUser);
    }

    @Transactional
    public void addStreakShield(User user, int count) {
        log.debug("Adding {} streak shields to user: {}", count, user.getEmail());
        user.setStreakShields(user.getStreakShields() + count);
        userRepository.save(user);
        log.info("Added {} streak shields to user: {}. Total: {}", count, user.getEmail(), user.getStreakShields());
    }

    @Transactional
    public boolean useStreakShield(User user) {
        log.debug("Attempting to use streak shield for user: {}", user.getEmail());

        if (user.getStreakShields() <= 0) {
            log.warn("User {} has no streak shields available", user.getEmail());
            return false;
        }

        user.setStreakShields(user.getStreakShields() - 1);
        userRepository.save(user);
        log.info("Streak shield used by user: {}. Remaining: {}", user.getEmail(), user.getStreakShields());
        return true;
    }
}
