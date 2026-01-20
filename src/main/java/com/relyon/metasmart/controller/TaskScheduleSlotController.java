package com.relyon.metasmart.controller;

import com.relyon.metasmart.constant.ApiPaths;
import com.relyon.metasmart.entity.actionplan.dto.ScheduleSlotRequest;
import com.relyon.metasmart.entity.actionplan.dto.ScheduleSlotResponse;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.service.TaskScheduleSlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(ApiPaths.GOALS + "/{goalId}" + ApiPaths.ACTION_ITEMS + "/{actionItemId}" + ApiPaths.SCHEDULE_SLOTS)
@RequiredArgsConstructor
@Tag(name = "Schedule Slots", description = "Drag & drop task scheduling")
public class TaskScheduleSlotController {

    private final TaskScheduleSlotService scheduleSlotService;

    @PostMapping
    @Operation(summary = "Create a schedule slot")
    public ResponseEntity<ScheduleSlotResponse> createSlot(
            @PathVariable Long goalId,
            @PathVariable Long actionItemId,
            @Valid @RequestBody ScheduleSlotRequest request,
            @AuthenticationPrincipal User user) {

        log.debug("Creating schedule slot for action item: {} in goal: {} by user: {}",
                actionItemId, goalId, user.getEmail());
        var result = scheduleSlotService.createSlot(goalId, actionItemId, request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping
    @Operation(summary = "Get all schedule slots for a task")
    public ResponseEntity<List<ScheduleSlotResponse>> getAllSlots(
            @PathVariable Long goalId,
            @PathVariable Long actionItemId,
            @AuthenticationPrincipal User user) {

        log.debug("Getting all schedule slots for action item: {} in goal: {} by user: {}",
                actionItemId, goalId, user.getEmail());
        var slots = scheduleSlotService.getAllSlots(goalId, actionItemId, user);
        return ResponseEntity.ok(slots);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active schedule slots for a task")
    public ResponseEntity<List<ScheduleSlotResponse>> getActiveSlots(
            @PathVariable Long goalId,
            @PathVariable Long actionItemId,
            @AuthenticationPrincipal User user) {

        log.debug("Getting active schedule slots for action item: {} in goal: {} by user: {}",
                actionItemId, goalId, user.getEmail());
        var slots = scheduleSlotService.getActiveSlots(goalId, actionItemId, user);
        return ResponseEntity.ok(slots);
    }

    @PutMapping("/{slotId}")
    @Operation(summary = "Update a schedule slot (reschedule)")
    public ResponseEntity<ScheduleSlotResponse> updateSlot(
            @PathVariable Long goalId,
            @PathVariable Long actionItemId,
            @PathVariable Long slotId,
            @Valid @RequestBody ScheduleSlotRequest request,
            @AuthenticationPrincipal User user) {

        log.debug("Updating schedule slot: {} for action item: {} in goal: {} by user: {}",
                slotId, actionItemId, goalId, user.getEmail());
        var result = scheduleSlotService.updateSlot(goalId, actionItemId, slotId, request, user);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{slotId}")
    @Operation(summary = "Delete a schedule slot")
    public ResponseEntity<Void> deleteSlot(
            @PathVariable Long goalId,
            @PathVariable Long actionItemId,
            @PathVariable Long slotId,
            @AuthenticationPrincipal User user) {

        log.debug("Deleting schedule slot: {} for action item: {} in goal: {} by user: {}",
                slotId, actionItemId, goalId, user.getEmail());
        scheduleSlotService.deleteSlot(goalId, actionItemId, slotId, user);
        return ResponseEntity.noContent().build();
    }
}
