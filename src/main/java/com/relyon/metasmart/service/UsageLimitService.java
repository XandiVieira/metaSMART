package com.relyon.metasmart.service;

import com.relyon.metasmart.entity.goal.GoalStatus;
import com.relyon.metasmart.entity.guardian.GuardianStatus;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.UsageLimitExceededException;
import com.relyon.metasmart.repository.GoalGuardianRepository;
import com.relyon.metasmart.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsageLimitService {

    private static final String LIMIT_ACTIVE_GOALS = "active goals";
    private static final String LIMIT_GUARDIANS_PER_GOAL = "guardians per goal";

    private final SubscriptionService subscriptionService;
    private final GoalRepository goalRepository;
    private final GoalGuardianRepository goalGuardianRepository;

    public void enforceGoalLimit(User user) {
        var entitlements = subscriptionService.getEntitlements(user);
        var maxGoals = entitlements.getMaxActiveGoals();
        var currentGoals = (int) goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.ACTIVE);

        log.debug("Checking goal limit for user {}: {}/{}", user.getId(), currentGoals, maxGoals);

        if (currentGoals >= maxGoals) {
            log.info("User {} exceeded goal limit: {}/{}", user.getId(), currentGoals, maxGoals);
            throw new UsageLimitExceededException(LIMIT_ACTIVE_GOALS, currentGoals, maxGoals);
        }
    }

    public void enforceGuardianLimit(User user, Long goalId) {
        var entitlements = subscriptionService.getEntitlements(user);
        var maxGuardians = entitlements.getMaxGuardiansPerGoal();

        var goal = new com.relyon.metasmart.entity.goal.Goal();
        goal.setId(goalId);

        var currentGuardians = (int) goalGuardianRepository.countByGoalAndStatus(goal, GuardianStatus.ACTIVE);
        var pendingGuardians = (int) goalGuardianRepository.countByGoalAndStatus(goal, GuardianStatus.PENDING);
        var totalGuardians = currentGuardians + pendingGuardians;

        log.debug("Checking guardian limit for goal {}: {}/{}", goalId, totalGuardians, maxGuardians);

        if (totalGuardians >= maxGuardians) {
            log.info("Goal {} exceeded guardian limit: {}/{}", goalId, totalGuardians, maxGuardians);
            throw new UsageLimitExceededException(LIMIT_GUARDIANS_PER_GOAL, totalGuardians, maxGuardians);
        }
    }

    public int getRemainingGoals(User user) {
        var entitlements = subscriptionService.getEntitlements(user);
        var maxGoals = entitlements.getMaxActiveGoals();
        var currentGoals = (int) goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.ACTIVE);
        return Math.max(0, maxGoals - currentGoals);
    }

    public int getRemainingGuardians(User user, Long goalId) {
        var entitlements = subscriptionService.getEntitlements(user);
        var maxGuardians = entitlements.getMaxGuardiansPerGoal();

        var goal = new com.relyon.metasmart.entity.goal.Goal();
        goal.setId(goalId);

        var currentGuardians = (int) goalGuardianRepository.countByGoalAndStatus(goal, GuardianStatus.ACTIVE);
        var pendingGuardians = (int) goalGuardianRepository.countByGoalAndStatus(goal, GuardianStatus.PENDING);
        var totalGuardians = currentGuardians + pendingGuardians;

        return Math.max(0, maxGuardians - totalGuardians);
    }
}
