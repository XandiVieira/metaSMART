package com.relyon.metasmart.service;

import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.goal.GoalStatus;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service responsible for managing goal lock/unlock state based on subscription tier limits.
 *
 * Lock Logic:
 * - When a user downgrades from premium, goals created during premium are locked (LIFO - newest first)
 * - Only goals exceeding the free tier limit are locked
 * - Goals created before premium are never locked
 *
 * Unlock Logic:
 * - When a user deletes a goal, locked goals may be unlocked to fill the freed slot
 * - When a user upgrades to premium, all locked goals are unlocked
 * - Unlocking follows FIFO order (oldest locked goal first)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoalLockService {

    private final GoalRepository goalRepository;
    private final SubscriptionService subscriptionService;

    /**
     * Recalculates the lock state for all goals of a user.
     * Should be called after:
     * - Goal deletion
     * - Goal reactivation
     * - Subscription change (upgrade/downgrade)
     */
    @Transactional
    public void recalculateLocksForUser(User user) {
        var entitlements = subscriptionService.getEntitlements(user);
        var maxGoals = entitlements.getMaxActiveGoals();
        var isPremium = Boolean.TRUE.equals(entitlements.getIsPremium());

        log.debug("Recalculating locks for user {}: maxGoals={}, isPremium={}",
                user.getId(), maxGoals, isPremium);

        if (isPremium) {
            // Premium users: unlock all locked goals
            unlockAllGoals(user);
        } else {
            // Free tier users: balance active vs locked goals
            balanceGoalLocks(user, maxGoals);
        }
    }

    /**
     * Unlocks all locked goals for a user (used when upgrading to premium).
     */
    @Transactional
    public void unlockAllGoals(User user) {
        var lockedGoals = goalRepository.findLockedGoalsOrderByCreatedAtAsc(user);

        if (lockedGoals.isEmpty()) {
            log.debug("No locked goals to unlock for user {}", user.getId());
            return;
        }

        for (var goal : lockedGoals) {
            unlockGoal(goal);
        }

        goalRepository.saveAll(lockedGoals);
        log.info("Unlocked {} goals for user {}", lockedGoals.size(), user.getId());
    }

    /**
     * Balances the number of active goals with the tier limit.
     * - If active < limit and locked goals exist: unlock oldest locked
     * - If active > limit: lock newest premium goals
     */
    @Transactional
    public void balanceGoalLocks(User user, int maxGoals) {
        var activeCount = (int) goalRepository.countActiveGoalsByOwner(user);
        var lockedGoals = goalRepository.findLockedGoalsOrderByCreatedAtAsc(user);

        log.debug("Balancing goals for user {}: active={}, locked={}, max={}",
                user.getId(), activeCount, lockedGoals.size(), maxGoals);

        if (activeCount < maxGoals && !lockedGoals.isEmpty()) {
            // Need to unlock some goals
            var slotsAvailable = maxGoals - activeCount;
            var goalsToUnlock = lockedGoals.stream()
                    .limit(slotsAvailable)
                    .toList();

            for (var goal : goalsToUnlock) {
                unlockGoal(goal);
            }

            goalRepository.saveAll(goalsToUnlock);
            log.info("Unlocked {} goals for user {} (filled available slots)",
                    goalsToUnlock.size(), user.getId());

        } else if (activeCount > maxGoals) {
            // Need to lock some goals (excess)
            var excess = activeCount - maxGoals;
            var premiumGoals = goalRepository.findPremiumGoalsForLocking(user);

            if (premiumGoals.isEmpty()) {
                log.warn("User {} has {} excess goals but no premium goals to lock",
                        user.getId(), excess);
                return;
            }

            var goalsToLock = premiumGoals.stream()
                    .limit(excess)
                    .toList();

            for (var goal : goalsToLock) {
                lockGoal(goal);
            }

            goalRepository.saveAll(goalsToLock);
            log.info("Locked {} goals for user {} (exceeded tier limit)",
                    goalsToLock.size(), user.getId());
        }
    }

    /**
     * Locks a single goal, preserving its previous status for later restoration.
     */
    private void lockGoal(Goal goal) {
        goal.setPreviousStatus(goal.getGoalStatus());
        goal.setGoalStatus(GoalStatus.LOCKED);
        log.debug("Locking goal {} (previous status: {})", goal.getId(), goal.getPreviousStatus());
    }

    /**
     * Unlocks a single goal, restoring its previous status.
     */
    private void unlockGoal(Goal goal) {
        var previousStatus = goal.getPreviousStatus();
        if (previousStatus != null && previousStatus != GoalStatus.LOCKED) {
            goal.setGoalStatus(previousStatus);
        } else {
            goal.setGoalStatus(GoalStatus.ACTIVE);
        }
        goal.setPreviousStatus(null);
        log.debug("Unlocking goal {} (restored status: {})", goal.getId(), goal.getGoalStatus());
    }

    /**
     * Checks if the user can create a new goal based on their current tier and active goal count.
     * Unlike enforceGoalLimit, this accounts for locked goals.
     */
    public boolean canCreateGoal(User user) {
        var entitlements = subscriptionService.getEntitlements(user);
        var maxGoals = entitlements.getMaxActiveGoals();
        var activeCount = (int) goalRepository.countActiveGoalsByOwner(user);
        return activeCount < maxGoals;
    }

    /**
     * Gets the number of remaining goal slots for a user.
     */
    public int getRemainingSlots(User user) {
        var entitlements = subscriptionService.getEntitlements(user);
        var maxGoals = entitlements.getMaxActiveGoals();
        var activeCount = (int) goalRepository.countActiveGoalsByOwner(user);
        return Math.max(0, maxGoals - activeCount);
    }
}
