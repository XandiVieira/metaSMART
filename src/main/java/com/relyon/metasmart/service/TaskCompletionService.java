package com.relyon.metasmart.service;

import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.entity.actionplan.ActionItem;
import com.relyon.metasmart.entity.actionplan.CompletionStatus;
import com.relyon.metasmart.entity.actionplan.TaskCompletion;
import com.relyon.metasmart.entity.actionplan.dto.TaskCompletionDto;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.mapper.TaskCompletionMapper;
import com.relyon.metasmart.repository.ActionItemRepository;
import com.relyon.metasmart.repository.GoalRepository;
import com.relyon.metasmart.repository.TaskCompletionRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskCompletionService {

    private final TaskCompletionRepository taskCompletionRepository;
    private final ActionItemRepository actionItemRepository;
    private final GoalRepository goalRepository;
    private final TaskCompletionMapper taskCompletionMapper;
    private final StreakService streakService;
    private final UserStreakService userStreakService;

    @Transactional
    public TaskCompletionDto recordCompletion(Long goalId, Long actionItemId, String note, User user) {
        log.debug("Recording completion for action item {} in goal {}", actionItemId, goalId);

        var actionItem = findActionItemByGoalAndUser(goalId, actionItemId, user);
        var today = LocalDate.now();

        var completion = TaskCompletion.builder()
                .actionItem(actionItem)
                .scheduledDate(today)
                .periodStart(calculatePeriodStart(today))
                .status(CompletionStatus.COMPLETED)
                .completedAt(LocalDateTime.now())
                .note(note)
                .build();

        var saved = taskCompletionRepository.save(completion);
        log.info("Recorded completion {} for action item {}", saved.getId(), actionItemId);

        streakService.updateStreakOnCompletion(user, actionItem, CompletionStatus.COMPLETED);
        userStreakService.onActivityRecorded(user);

        return taskCompletionMapper.toDto(saved);
    }

    @Transactional
    public TaskCompletionDto recordCompletionForDate(Long goalId, Long actionItemId, LocalDate date, String note, User user) {
        log.debug("Recording completion for action item {} on date {}", actionItemId, date);

        var actionItem = findActionItemByGoalAndUser(goalId, actionItemId, user);
        var periodStart = calculatePeriodStart(date);

        log.debug("Creating TaskCompletion: actionItemId={}, scheduledDate={}, periodStart={}, status=COMPLETED",
                actionItemId, date, periodStart);

        var completion = TaskCompletion.builder()
                .actionItem(actionItem)
                .scheduledDate(date)
                .periodStart(periodStart)
                .status(CompletionStatus.COMPLETED)
                .completedAt(LocalDateTime.now())
                .note(note)
                .build();

        TaskCompletion saved;
        try {
            saved = taskCompletionRepository.save(completion);
            log.info("Recorded completion {} for action item {} on date {}", saved.getId(), actionItemId, date);
        } catch (Exception e) {
            log.error("Failed to save TaskCompletion (actionItemId={}, scheduledDate={}, periodStart={}): {}",
                    actionItemId, date, periodStart, e.getMessage(), e);
            throw e;
        }

        try {
            streakService.updateStreakOnCompletion(user, actionItem, CompletionStatus.COMPLETED);
        } catch (Exception e) {
            log.error("Failed to update streak for user {} on action item {}: {}", user.getEmail(), actionItemId, e.getMessage(), e);
            throw e;
        }

        try {
            userStreakService.onActivityRecorded(user);
        } catch (Exception e) {
            log.error("Failed to record activity for user {}: {}", user.getEmail(), e.getMessage(), e);
            throw e;
        }

        return taskCompletionMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<TaskCompletionDto> getCompletionHistory(Long goalId, Long actionItemId, User user) {
        log.debug("Getting completion history for action item {}", actionItemId);

        var actionItem = findActionItemByGoalAndUser(goalId, actionItemId, user);

        return taskCompletionRepository.findByActionItemOrderByCompletedAtDesc(actionItem).stream()
                .map(taskCompletionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<TaskCompletionDto> getCompletionHistoryPaginated(Long goalId, Long actionItemId, User user, Pageable pageable) {
        log.debug("Getting paginated completion history for action item {}", actionItemId);

        var actionItem = findActionItemByGoalAndUser(goalId, actionItemId, user);

        return taskCompletionRepository.findByActionItemOrderByCompletedAtDesc(actionItem, pageable)
                .map(taskCompletionMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<TaskCompletionDto> getCompletionsByDateRange(Long goalId, Long actionItemId, LocalDate startDate, LocalDate endDate, User user) {
        log.debug("Getting completions for action item {} between {} and {}", actionItemId, startDate, endDate);

        var actionItem = findActionItemByGoalAndUser(goalId, actionItemId, user);

        return taskCompletionRepository.findByActionItemAndScheduledDateBetween(actionItem, startDate, endDate).stream()
                .map(taskCompletionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countCompletions(Long goalId, Long actionItemId, User user) {
        var actionItem = findActionItemByGoalAndUser(goalId, actionItemId, user);
        return taskCompletionRepository.countByActionItem(actionItem);
    }

    @Transactional(readOnly = true)
    public long countCompletionsInPeriod(Long goalId, Long actionItemId, LocalDate startDate, LocalDate endDate, User user) {
        var actionItem = findActionItemByGoalAndUser(goalId, actionItemId, user);
        return taskCompletionRepository.countByActionItemAndScheduledDateBetween(actionItem, startDate, endDate);
    }

    @Transactional
    public void deleteCompletion(Long goalId, Long actionItemId, Long completionId, User user) {
        log.debug("Deleting completion {} for action item {}", completionId, actionItemId);

        findActionItemByGoalAndUser(goalId, actionItemId, user);

        var completion = taskCompletionRepository.findById(completionId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.TASK_COMPLETION_NOT_FOUND));

        taskCompletionRepository.delete(completion);
        log.info("Deleted completion {}", completionId);
    }

    private ActionItem findActionItemByGoalAndUser(Long goalId, Long actionItemId, User user) {
        var goal = goalRepository.findByIdAndOwner(goalId, user)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND));

        return actionItemRepository.findByIdAndGoal(actionItemId, goal)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.ACTION_ITEM_NOT_FOUND));
    }

    private LocalDate calculatePeriodStart(LocalDate date) {
        var weekFields = WeekFields.of(Locale.getDefault());
        return date.with(weekFields.dayOfWeek(), 1);
    }
}
