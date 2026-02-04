package com.relyon.metasmart.service;

import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.entity.actionplan.ScheduledTask;
import com.relyon.metasmart.entity.actionplan.TaskType;
import com.relyon.metasmart.entity.actionplan.dto.ScheduledTaskDto;
import com.relyon.metasmart.entity.actionplan.dto.ScheduledTaskRequest;
import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.DuplicateResourceException;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.mapper.ScheduledTaskMapper;
import com.relyon.metasmart.repository.ActionItemRepository;
import com.relyon.metasmart.repository.GoalRepository;
import com.relyon.metasmart.repository.ScheduledTaskRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledTaskService {

    private final ScheduledTaskRepository scheduledTaskRepository;
    private final ActionItemRepository actionItemRepository;
    private final GoalRepository goalRepository;
    private final ScheduledTaskMapper scheduledTaskMapper;
    private final TaskCompletionService taskCompletionService;

    @Setter(onMethod_ = {@Autowired, @Lazy})
    private ScheduledTaskService self;

    @Transactional
    public ScheduledTaskDto createScheduledTask(Long goalId, ScheduledTaskRequest request, User user) {
        log.debug("Creating scheduled task for goal {} on date {}", goalId, request.getScheduledDate());

        var goal = findGoalByUser(goalId, user);
        var actionItem = actionItemRepository.findByIdAndGoal(request.getTaskId(), goal)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.ACTION_ITEM_NOT_FOUND));

        if (scheduledTaskRepository.findByActionItemAndScheduledDate(actionItem, request.getScheduledDate()).isPresent()) {
            throw new DuplicateResourceException(ErrorMessages.SCHEDULED_TASK_ALREADY_EXISTS);
        }

        var scheduledTask = ScheduledTask.builder()
                .actionItem(actionItem)
                .scheduledDate(request.getScheduledDate())
                .completed(false)
                .build();

        var saved = scheduledTaskRepository.save(scheduledTask);
        log.info("Created scheduled task {} for action item {} on {}", saved.getId(), actionItem.getId(), request.getScheduledDate());

        return scheduledTaskMapper.toDto(saved);
    }

    @Transactional
    public List<ScheduledTaskDto> createBulkScheduledTasks(Long goalId, Long actionItemId, List<LocalDate> dates, User user) {
        log.debug("Creating {} scheduled tasks for action item {}", dates.size(), actionItemId);

        var goal = findGoalByUser(goalId, user);
        var actionItem = actionItemRepository.findByIdAndGoal(actionItemId, goal)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.ACTION_ITEM_NOT_FOUND));

        var results = new ArrayList<ScheduledTaskDto>();

        for (var date : dates) {
            if (scheduledTaskRepository.findByActionItemAndScheduledDate(actionItem, date).isEmpty()) {
                var scheduledTask = ScheduledTask.builder()
                        .actionItem(actionItem)
                        .scheduledDate(date)
                        .completed(false)
                        .build();

                var saved = scheduledTaskRepository.save(scheduledTask);
                results.add(scheduledTaskMapper.toDto(saved));
            }
        }

        log.info("Created {} scheduled tasks for action item {}", results.size(), actionItemId);
        return results;
    }

    @Transactional
    public List<ScheduledTaskDto> generateScheduleForFrequencyTask(Long goalId, Long actionItemId, LocalDate startDate, LocalDate endDate, User user) {
        log.debug("Generating schedule for frequency task {} from {} to {}", actionItemId, startDate, endDate);

        var goal = findGoalByUser(goalId, user);
        var actionItem = actionItemRepository.findByIdAndGoal(actionItemId, goal)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.ACTION_ITEM_NOT_FOUND));

        if (actionItem.getTaskType() != TaskType.FREQUENCY_BASED || actionItem.getFrequencyGoal() == null) {
            log.warn("Action item {} is not a frequency-based task", actionItemId);
            return List.of();
        }

        var frequencyGoal = actionItem.getFrequencyGoal();
        var dates = calculateScheduleDates(startDate, endDate, frequencyGoal.getCount(), frequencyGoal.getPeriod(), frequencyGoal.getFixedDays());

        return self.createBulkScheduledTasks(goalId, actionItemId, dates, user);
    }

    @Transactional(readOnly = true)
    public List<ScheduledTaskDto> getScheduledTasksByGoal(Long goalId, User user) {
        log.debug("Getting all scheduled tasks for goal {}", goalId);

        var goal = findGoalByUser(goalId, user);
        return scheduledTaskRepository.findByGoalOrderByScheduledDateAsc(goal).stream()
                .map(scheduledTaskMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ScheduledTaskDto> getScheduledTasksByDateRange(Long goalId, LocalDate startDate, LocalDate endDate, User user) {
        log.debug("Getting scheduled tasks for goal {} between {} and {}", goalId, startDate, endDate);

        var goal = findGoalByUser(goalId, user);
        return scheduledTaskRepository.findByGoalAndDateRange(goal, startDate, endDate).stream()
                .map(scheduledTaskMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ScheduledTaskDto> getScheduledTasksByActionItem(Long goalId, Long actionItemId, User user) {
        log.debug("Getting scheduled tasks for action item {}", actionItemId);

        var goal = findGoalByUser(goalId, user);
        var actionItem = actionItemRepository.findByIdAndGoal(actionItemId, goal)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.ACTION_ITEM_NOT_FOUND));

        return scheduledTaskRepository.findByActionItemOrderByScheduledDateAsc(actionItem).stream()
                .map(scheduledTaskMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ScheduledTaskDto> getPendingTasks(Long goalId, User user) {
        log.debug("Getting pending scheduled tasks for goal {}", goalId);

        var goal = findGoalByUser(goalId, user);
        return scheduledTaskRepository.findPendingByGoalUntilDate(goal, LocalDate.now()).stream()
                .map(scheduledTaskMapper::toDto)
                .toList();
    }

    @Transactional
    public ScheduledTaskDto markAsCompleted(Long goalId, Long scheduledTaskId, User user) {
        log.debug("Marking scheduled task {} as completed", scheduledTaskId);

        findGoalByUser(goalId, user);

        var scheduledTask = scheduledTaskRepository.findByIdWithActionItem(scheduledTaskId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SCHEDULED_TASK_NOT_FOUND));

        scheduledTask.setCompleted(true);
        scheduledTask.setCompletedAt(LocalDateTime.now());

        var saved = scheduledTaskRepository.save(scheduledTask);
        log.info("Marked scheduled task {} as completed", scheduledTaskId);

        taskCompletionService.recordCompletionForDate(
                goalId,
                scheduledTask.getActionItem().getId(),
                scheduledTask.getScheduledDate(),
                null,
                user
        );

        return scheduledTaskMapper.toDto(saved);
    }

    @Transactional
    public ScheduledTaskDto markAsIncomplete(Long goalId, Long scheduledTaskId, User user) {
        log.debug("Marking scheduled task {} as incomplete", scheduledTaskId);

        findGoalByUser(goalId, user);

        var scheduledTask = scheduledTaskRepository.findById(scheduledTaskId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SCHEDULED_TASK_NOT_FOUND));

        scheduledTask.setCompleted(false);
        scheduledTask.setCompletedAt(null);

        var saved = scheduledTaskRepository.save(scheduledTask);
        log.info("Marked scheduled task {} as incomplete", scheduledTaskId);

        return scheduledTaskMapper.toDto(saved);
    }

    @Transactional
    public void deleteScheduledTask(Long goalId, Long scheduledTaskId, User user) {
        log.debug("Deleting scheduled task {}", scheduledTaskId);

        findGoalByUser(goalId, user);

        var scheduledTask = scheduledTaskRepository.findById(scheduledTaskId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SCHEDULED_TASK_NOT_FOUND));

        scheduledTaskRepository.delete(scheduledTask);
        log.info("Deleted scheduled task {}", scheduledTaskId);
    }

    private Goal findGoalByUser(Long goalId, User user) {
        return goalRepository.findByIdAndOwner(goalId, user)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND));
    }

    private List<LocalDate> calculateScheduleDates(LocalDate startDate, LocalDate endDate, int count,
                                                   com.relyon.metasmart.entity.actionplan.FrequencyPeriod period, String fixedDaysStr) {
        var dates = new ArrayList<LocalDate>();

        if (fixedDaysStr != null && !fixedDaysStr.isBlank()) {
            var fixedDays = java.util.Arrays.stream(fixedDaysStr.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .toList();

            var current = startDate;
            while (!current.isAfter(endDate)) {
                if (fixedDays.contains(current.getDayOfWeek().getValue() % 7)) {
                    dates.add(current);
                }
                current = current.plusDays(1);
            }
        } else {
            var periodDays = period == com.relyon.metasmart.entity.actionplan.FrequencyPeriod.WEEK ? 7 : 30;
            var interval = periodDays / count;

            var current = startDate;
            while (!current.isAfter(endDate)) {
                dates.add(current);
                current = current.plusDays(interval);
            }
        }

        return dates;
    }
}
