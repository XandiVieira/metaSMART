package com.relyon.metasmart.service;

import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.entity.actionplan.ActionItem;
import com.relyon.metasmart.entity.actionplan.TaskScheduleSlot;
import com.relyon.metasmart.entity.actionplan.dto.ScheduleSlotRequest;
import com.relyon.metasmart.entity.actionplan.dto.ScheduleSlotResponse;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.DuplicateResourceException;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.mapper.TaskScheduleSlotMapper;
import com.relyon.metasmart.repository.ActionItemRepository;
import com.relyon.metasmart.repository.GoalRepository;
import com.relyon.metasmart.repository.TaskScheduleSlotRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskScheduleSlotService {

    private final TaskScheduleSlotRepository scheduleSlotRepository;
    private final ActionItemRepository actionItemRepository;
    private final GoalRepository goalRepository;
    private final TaskScheduleSlotMapper scheduleSlotMapper;

    @Transactional
    public ScheduleSlotResponse createSlot(Long goalId, Long actionItemId, ScheduleSlotRequest request, User user) {
        log.debug("Creating schedule slot for action item {} in goal {} by user {}", actionItemId, goalId, user.getEmail());

        var actionItem = findActionItemByGoalAndUser(goalId, actionItemId, user);

        var existingSlots = scheduleSlotRepository.findActiveSlotByIndex(
                actionItem, request.getSlotIndex(), LocalDate.now());
        if (!existingSlots.isEmpty()) {
            throw new DuplicateResourceException(ErrorMessages.SCHEDULE_SLOT_ALREADY_EXISTS);
        }

        var slot = TaskScheduleSlot.builder()
                .actionItem(actionItem)
                .slotIndex(request.getSlotIndex())
                .specificTime(request.getSpecificTime())
                .createdVia(request.getCreatedVia())
                .effectiveFrom(LocalDate.now())
                .build();

        var saved = scheduleSlotRepository.save(slot);
        log.info("Created schedule slot {} for action item {}", saved.getId(), actionItemId);

        return scheduleSlotMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ScheduleSlotResponse> getAllSlots(Long goalId, Long actionItemId, User user) {
        log.debug("Getting all schedule slots for action item {} in goal {}", actionItemId, goalId);

        var actionItem = findActionItemByGoalAndUser(goalId, actionItemId, user);

        return scheduleSlotRepository.findByActionItemOrderBySlotIndexAsc(actionItem).stream()
                .map(scheduleSlotMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ScheduleSlotResponse> getActiveSlots(Long goalId, Long actionItemId, User user) {
        log.debug("Getting active schedule slots for action item {} in goal {}", actionItemId, goalId);

        var actionItem = findActionItemByGoalAndUser(goalId, actionItemId, user);

        return scheduleSlotRepository.findActiveSlots(actionItem).stream()
                .map(scheduleSlotMapper::toResponse)
                .toList();
    }

    @Transactional
    public ScheduleSlotResponse updateSlot(Long goalId, Long actionItemId, Long slotId, ScheduleSlotRequest request, User user) {
        log.debug("Updating schedule slot {} for action item {} in goal {}", slotId, actionItemId, goalId);

        var actionItem = findActionItemByGoalAndUser(goalId, actionItemId, user);
        var existingSlot = scheduleSlotRepository.findById(slotId)
                .filter(s -> s.getActionItem().getId().equals(actionItem.getId()))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SCHEDULE_SLOT_NOT_FOUND));

        existingSlot.setEffectiveUntil(LocalDate.now().minusDays(1));
        scheduleSlotRepository.save(existingSlot);

        var newSlot = TaskScheduleSlot.builder()
                .actionItem(actionItem)
                .slotIndex(request.getSlotIndex())
                .specificTime(request.getSpecificTime())
                .createdVia(request.getCreatedVia())
                .effectiveFrom(LocalDate.now())
                .rescheduledFromSlot(existingSlot)
                .rescheduleReason(request.getRescheduleReason())
                .build();

        var saved = scheduleSlotRepository.save(newSlot);
        log.info("Updated schedule slot {} to new slot {} for action item {}", slotId, saved.getId(), actionItemId);

        return scheduleSlotMapper.toResponse(saved);
    }

    @Transactional
    public void deleteSlot(Long goalId, Long actionItemId, Long slotId, User user) {
        log.debug("Deleting schedule slot {} for action item {} in goal {}", slotId, actionItemId, goalId);

        var actionItem = findActionItemByGoalAndUser(goalId, actionItemId, user);
        var slot = scheduleSlotRepository.findById(slotId)
                .filter(s -> s.getActionItem().getId().equals(actionItem.getId()))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SCHEDULE_SLOT_NOT_FOUND));

        slot.setEffectiveUntil(LocalDate.now().minusDays(1));
        scheduleSlotRepository.save(slot);

        log.info("Soft deleted schedule slot {} for action item {}", slotId, actionItemId);
    }

    private ActionItem findActionItemByGoalAndUser(Long goalId, Long actionItemId, User user) {
        var goal = goalRepository.findByIdAndOwner(goalId, user)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND));

        return actionItemRepository.findByIdAndGoal(actionItemId, goal)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.ACTION_ITEM_NOT_FOUND));
    }
}
