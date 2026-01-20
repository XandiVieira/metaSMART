package com.relyon.metasmart.service;

import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.entity.actionplan.ActionItem;
import com.relyon.metasmart.entity.actionplan.CompletionStatus;
import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.streak.StreakInfo;
import com.relyon.metasmart.entity.streak.dto.StreakResponse;
import com.relyon.metasmart.entity.streak.dto.StreakSummaryResponse;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.mapper.StreakMapper;
import com.relyon.metasmart.repository.ActionItemRepository;
import com.relyon.metasmart.repository.GoalRepository;
import com.relyon.metasmart.repository.StreakInfoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StreakService {

    private final StreakInfoRepository streakInfoRepository;
    private final GoalRepository goalRepository;
    private final ActionItemRepository actionItemRepository;
    private final StreakMapper streakMapper;

    @Transactional(readOnly = true)
    public StreakSummaryResponse getUserStreakSummary(User user) {
        log.debug("Getting streak summary for user: {}", user.getEmail());

        var userStreak = streakInfoRepository.findByUserAndGoalIsNullAndActionItemIsNull(user)
                .map(streakMapper::toResponse)
                .orElse(createDefaultStreakResponse(null, null));

        var goalStreaks = streakInfoRepository.findGoalStreaksByUser(user).stream()
                .map(streakMapper::toResponse)
                .toList();

        var taskStreaks = streakInfoRepository.findTaskStreaksByUser(user).stream()
                .map(streakMapper::toResponse)
                .toList();

        return StreakSummaryResponse.builder()
                .userStreak(userStreak)
                .goalStreaks(goalStreaks)
                .taskStreaks(taskStreaks)
                .build();
    }

    @Transactional(readOnly = true)
    public StreakResponse getUserStreak(User user) {
        log.debug("Getting user-level streak for user: {}", user.getEmail());

        return streakInfoRepository.findByUserAndGoalIsNullAndActionItemIsNull(user)
                .map(streakMapper::toResponse)
                .orElse(createDefaultStreakResponse(null, null));
    }

    @Transactional(readOnly = true)
    public StreakResponse getGoalStreak(Long goalId, User user) {
        log.debug("Getting streak for goal: {} by user: {}", goalId, user.getEmail());

        var goal = goalRepository.findByIdAndOwner(goalId, user)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND));

        return streakInfoRepository.findByUserAndGoalAndActionItemIsNull(user, goal)
                .map(streakMapper::toResponse)
                .orElse(createDefaultStreakResponse(goalId, null));
    }

    @Transactional(readOnly = true)
    public StreakResponse getTaskStreak(Long goalId, Long actionItemId, User user) {
        log.debug("Getting streak for task: {} in goal: {} by user: {}", actionItemId, goalId, user.getEmail());

        var goal = goalRepository.findByIdAndOwner(goalId, user)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND));

        var actionItem = actionItemRepository.findByIdAndGoal(actionItemId, goal)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.ACTION_ITEM_NOT_FOUND));

        return streakInfoRepository.findByUserAndActionItem(user, actionItem)
                .map(streakMapper::toResponse)
                .orElse(createDefaultStreakResponse(goalId, actionItemId));
    }

    @Transactional(readOnly = true)
    public List<StreakResponse> getGoalStreaks(Long goalId, User user) {
        log.debug("Getting all streaks for goal: {} by user: {}", goalId, user.getEmail());

        var goal = goalRepository.findByIdAndOwner(goalId, user)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND));

        return streakInfoRepository.findByGoal(goal).stream()
                .map(streakMapper::toResponse)
                .toList();
    }

    @Transactional
    public void updateStreakOnCompletion(User user, ActionItem actionItem, CompletionStatus status) {
        log.debug("Updating streak for user: {}, task: {}, status: {}", user.getEmail(), actionItem.getId(), status);

        updateTaskStreak(user, actionItem, status);
        updateGoalStreak(user, actionItem.getGoal(), status);
        updateUserStreak(user, status);
    }

    private void updateTaskStreak(User user, ActionItem actionItem, CompletionStatus status) {
        var streak = streakInfoRepository.findByUserAndActionItem(user, actionItem)
                .orElseGet(() -> createNewStreak(user, actionItem.getGoal(), actionItem));

        updateStreakBasedOnStatus(streak, status);
        streakInfoRepository.save(streak);
    }

    private void updateGoalStreak(User user, Goal goal, CompletionStatus status) {
        var streak = streakInfoRepository.findByUserAndGoalAndActionItemIsNull(user, goal)
                .orElseGet(() -> createNewStreak(user, goal, null));

        updateStreakBasedOnStatus(streak, status);
        streakInfoRepository.save(streak);
    }

    private void updateUserStreak(User user, CompletionStatus status) {
        var streak = streakInfoRepository.findByUserAndGoalIsNullAndActionItemIsNull(user)
                .orElseGet(() -> createNewStreak(user, null, null));

        updateStreakBasedOnStatus(streak, status);
        streakInfoRepository.save(streak);
    }

    private void updateStreakBasedOnStatus(StreakInfo streak, CompletionStatus status) {
        switch (status) {
            case COMPLETED -> {
                streak.incrementMaintainedStreak();
                streak.incrementPerfectStreak();
            }
            case PARTIAL -> {
                streak.incrementMaintainedStreak();
                streak.resetPerfectStreak();
            }
            case MISSED -> {
                streak.resetMaintainedStreak();
                streak.resetPerfectStreak();
            }
            case RESCHEDULED, PENDING -> {
                // No streak change for rescheduled or pending
            }
        }
    }

    private StreakInfo createNewStreak(User user, Goal goal, ActionItem actionItem) {
        return StreakInfo.builder()
                .user(user)
                .goal(goal)
                .actionItem(actionItem)
                .currentMaintainedStreak(0)
                .bestMaintainedStreak(0)
                .currentPerfectStreak(0)
                .bestPerfectStreak(0)
                .build();
    }

    private StreakResponse createDefaultStreakResponse(Long goalId, Long actionItemId) {
        String level;
        if (goalId == null && actionItemId == null) {
            level = "USER";
        } else if (actionItemId == null) {
            level = "GOAL";
        } else {
            level = "TASK";
        }

        return StreakResponse.builder()
                .goalId(goalId)
                .actionItemId(actionItemId)
                .currentMaintainedStreak(0)
                .bestMaintainedStreak(0)
                .currentPerfectStreak(0)
                .bestPerfectStreak(0)
                .level(level)
                .build();
    }
}
