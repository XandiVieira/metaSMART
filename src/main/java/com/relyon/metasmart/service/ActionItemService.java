package com.relyon.metasmart.service;

import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.entity.actionplan.dto.ActionItemRequest;
import com.relyon.metasmart.entity.actionplan.dto.ActionItemResponse;
import com.relyon.metasmart.entity.actionplan.dto.UpdateActionItemRequest;
import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.mapper.ActionItemMapper;
import com.relyon.metasmart.repository.ActionItemRepository;
import com.relyon.metasmart.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActionItemService {

    private final ActionItemRepository actionItemRepository;
    private final GoalRepository goalRepository;
    private final ActionItemMapper actionItemMapper;

    @Transactional
    public ActionItemResponse create(Long goalId, ActionItemRequest request, User user) {
        log.debug("Creating action item for goal ID: {}", goalId);

        var goal = findGoalByIdAndOwner(goalId, user);
        var actionItem = actionItemMapper.toEntity(request);
        actionItem.setGoal(goal);

        var savedItem = actionItemRepository.save(actionItem);
        log.info("Action item created with ID: {} for goal ID: {}", savedItem.getId(), goalId);

        return actionItemMapper.toResponse(savedItem);
    }

    @Transactional(readOnly = true)
    public List<ActionItemResponse> findByGoal(Long goalId, User user) {
        log.debug("Fetching action items for goal ID: {}", goalId);

        var goal = findGoalByIdAndOwner(goalId, user);
        return actionItemRepository.findByGoalOrderByOrderIndexAscCreatedAtAsc(goal).stream()
                .map(actionItemMapper::toResponse)
                .toList();
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
        Optional.ofNullable(request.getDueDate()).ifPresent(actionItem::setDueDate);
        Optional.ofNullable(request.getOrderIndex()).ifPresent(actionItem::setOrderIndex);
        Optional.ofNullable(request.getCompleted()).ifPresent(actionItem::setCompleted);

        var savedItem = actionItemRepository.save(actionItem);
        log.info("Action item updated with ID: {} for goal ID: {}", savedItem.getId(), goalId);

        return actionItemMapper.toResponse(savedItem);
    }

    @Transactional
    public void delete(Long goalId, Long itemId, User user) {
        log.debug("Deleting action item ID: {} from goal ID: {}", itemId, goalId);

        var goal = findGoalByIdAndOwner(goalId, user);
        var actionItem = actionItemRepository.findByIdAndGoal(itemId, goal)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.ACTION_ITEM_NOT_FOUND));

        actionItemRepository.delete(actionItem);
        log.info("Action item ID: {} deleted from goal ID: {}", itemId, goalId);
    }

    private Goal findGoalByIdAndOwner(Long goalId, User user) {
        return goalRepository.findByIdAndOwner(goalId, user)
                .orElseThrow(() -> {
                    log.warn("Goal not found with ID: {} for user ID: {}", goalId, user.getId());
                    return new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND);
                });
    }
}
