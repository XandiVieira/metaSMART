package com.relyon.metasmart.service;

import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.constant.LogMessages;
import com.relyon.metasmart.entity.actionplan.ActionItem;
import com.relyon.metasmart.entity.actionplan.dto.ActionItemRequest;
import com.relyon.metasmart.entity.actionplan.dto.ActionItemResponse;
import com.relyon.metasmart.entity.actionplan.dto.TaskCompletionDto;
import com.relyon.metasmart.entity.actionplan.dto.UpdateActionItemRequest;
import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.mapper.ActionItemMapper;
import com.relyon.metasmart.mapper.TaskCompletionMapper;
import com.relyon.metasmart.repository.ActionItemRepository;
import com.relyon.metasmart.repository.GoalRepository;
import com.relyon.metasmart.repository.ScheduledTaskRepository;
import com.relyon.metasmart.repository.StreakInfoRepository;
import com.relyon.metasmart.repository.TaskCompletionRepository;
import com.relyon.metasmart.repository.TaskScheduleSlotRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActionItemService {

    private final ActionItemRepository actionItemRepository;
    private final GoalRepository goalRepository;
    private final TaskCompletionRepository taskCompletionRepository;
    private final ScheduledTaskRepository scheduledTaskRepository;
    private final TaskScheduleSlotRepository taskScheduleSlotRepository;
    private final StreakInfoRepository streakInfoRepository;
    private final ActionItemMapper actionItemMapper;
    private final TaskCompletionMapper taskCompletionMapper;

    @Transactional
    public ActionItemResponse create(Long goalId, ActionItemRequest request, User user) {
        log.debug("Creating action item for goal ID: {}", goalId);

        var goal = findGoalByIdAndOwner(goalId, user);
        var actionItem = actionItemMapper.toEntity(request);
        actionItem.setGoal(goal);

        var savedItem = actionItemRepository.save(actionItem);
        log.info("Action item created with ID: {} for goal ID: {}", savedItem.getId(), goalId);

        return toResponseWithCompletionHistory(savedItem);
    }

    @Transactional(readOnly = true)
    public List<ActionItemResponse> findByGoal(Long goalId, User user) {
        log.debug("Fetching action items for goal ID: {}", goalId);

        var goal = findGoalByIdAndOwner(goalId, user);
        return actionItemRepository.findByGoalOrderByOrderIndexAscCreatedAtAsc(goal).stream()
                .map(this::toResponseWithCompletionHistory)
                .toList();
    }

    @Transactional(readOnly = true)
    public ActionItemResponse findById(Long goalId, Long itemId, User user) {
        log.debug("Fetching action item ID: {} for goal ID: {}", itemId, goalId);

        var goal = findGoalByIdAndOwner(goalId, user);
        var actionItem = actionItemRepository.findByIdAndGoal(itemId, goal)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.ACTION_ITEM_NOT_FOUND));

        return toResponseWithCompletionHistory(actionItem);
    }

    @Transactional
    public ActionItemResponse update(Long goalId, Long itemId, UpdateActionItemRequest request, User user) {
        log.debug("Updating action item ID: {} for goal ID: {}", itemId, goalId);

        var goal = findGoalByIdAndOwner(goalId, user);
        var actionItem = actionItemRepository.findByIdAndGoal(itemId, goal)
                .orElseThrow(() -> {
                    log.warn("Action item not found with ID: {} for goal ID: {}", itemId, goalId);
                    return new ResourceNotFoundException(ErrorMessages.ACTION_ITEM_NOT_FOUND);
                });

        Optional.ofNullable(request.getTitle()).ifPresent(actionItem::setTitle);
        Optional.ofNullable(request.getDescription()).ifPresent(actionItem::setDescription);
        Optional.ofNullable(request.getTaskType()).ifPresent(actionItem::setTaskType);
        Optional.ofNullable(request.getPriority()).ifPresent(actionItem::setPriority);
        Optional.ofNullable(request.getOrderIndex()).ifPresent(actionItem::setOrderIndex);
        Optional.ofNullable(request.getImpactScore()).ifPresent(actionItem::setImpactScore);
        Optional.ofNullable(request.getEffortEstimate()).ifPresent(actionItem::setEffortEstimate);
        Optional.ofNullable(request.getNotes()).ifPresent(actionItem::setNotes);

        Optional.ofNullable(request.getTargetDate()).ifPresent(actionItem::setTargetDate);

        if (request.getContext() != null) {
            actionItem.setContext(String.join(",", request.getContext()));
        }
        if (request.getDependencies() != null) {
            actionItem.setDependencies(request.getDependencies().stream()
                    .map(String::valueOf)
                    .reduce((a, b) -> a + "," + b)
                    .orElse(null));
        }

        if (request.getRecurrence() != null) {
            actionItem.setRecurrence(actionItemMapper.toRecurrenceEntity(request.getRecurrence()));
        }
        if (request.getFrequencyGoal() != null) {
            actionItem.setFrequencyGoal(actionItemMapper.toFrequencyGoalEntity(request.getFrequencyGoal()));
        }
        if (request.getRemindersOverride() != null) {
            actionItem.setReminderOverride(actionItemMapper.toReminderOverrideEntity(request.getRemindersOverride()));
        }

        if (request.getCompleted() != null) {
            actionItem.setCompleted(request.getCompleted());
            if (Boolean.TRUE.equals(request.getCompleted()) && actionItem.getCompletedAt() == null) {
                actionItem.setCompletedAt(LocalDateTime.now());
            } else if (Boolean.FALSE.equals(request.getCompleted())) {
                actionItem.setCompletedAt(null);
            }
        }

        var savedItem = actionItemRepository.save(actionItem);
        log.info("Action item updated with ID: {} for goal ID: {}", savedItem.getId(), goalId);

        return toResponseWithCompletionHistory(savedItem);
    }

    @Transactional
    public void delete(Long goalId, Long itemId, User user) {
        log.debug("Deleting action item ID: {} from goal ID: {}", itemId, goalId);

        var goal = findGoalByIdAndOwner(goalId, user);
        var actionItem = actionItemRepository.findByIdAndGoal(itemId, goal)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.ACTION_ITEM_NOT_FOUND));

        streakInfoRepository.deleteByActionItem(actionItem);
        scheduledTaskRepository.deleteByActionItem(actionItem);
        taskCompletionRepository.deleteByActionItem(actionItem);
        taskScheduleSlotRepository.deleteByActionItem(actionItem);
        actionItemRepository.delete(actionItem);
        log.info("Action item ID: {} deleted from goal ID: {}", itemId, goalId);
    }

    private Goal findGoalByIdAndOwner(Long goalId, User user) {
        return goalRepository.findByIdAndOwner(goalId, user)
                .orElseThrow(() -> {
                    log.warn(LogMessages.GOAL_NOT_FOUND_FOR_USER, goalId, user.getId());
                    return new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND);
                });
    }

    private ActionItemResponse toResponseWithCompletionHistory(ActionItem actionItem) {
        var response = actionItemMapper.toResponse(actionItem);

        List<TaskCompletionDto> completionHistory = taskCompletionRepository
                .findByActionItemOrderByCompletedAtDesc(actionItem).stream()
                .map(taskCompletionMapper::toDto)
                .toList();

        response.setCompletionHistory(completionHistory);
        return response;
    }
}
