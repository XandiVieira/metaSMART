package com.relyon.metasmart.controller;

import com.relyon.metasmart.constant.ApiPaths;
import com.relyon.metasmart.entity.actionplan.dto.ScheduledTaskDto;
import com.relyon.metasmart.entity.actionplan.dto.ScheduledTaskRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.service.ScheduledTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(ApiPaths.GOALS + "/{goalId}" + ApiPaths.SCHEDULED_TASKS)
@RequiredArgsConstructor
@Tag(name = "Scheduled Tasks (Flight Plan)", description = "Schedule and manage task instances")
public class ScheduledTaskController {

    private final ScheduledTaskService scheduledTaskService;

    @PostMapping
    @Operation(summary = "Create a scheduled task instance")
    public ResponseEntity<ScheduledTaskDto> createScheduledTask(
            @PathVariable Long goalId,
            @Valid @RequestBody ScheduledTaskRequest request,
            @AuthenticationPrincipal User user) {

        log.debug("Creating scheduled task for goal: {} by user: {}", goalId, user.getEmail());
        var result = scheduledTaskService.createScheduledTask(goalId, request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/generate/{actionItemId}")
    @Operation(summary = "Auto-generate schedule for a frequency-based task")
    public ResponseEntity<List<ScheduledTaskDto>> generateSchedule(
            @PathVariable Long goalId,
            @PathVariable Long actionItemId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal User user) {

        log.debug("Generating schedule for action item: {} in goal: {} from {} to {} by user: {}",
                actionItemId, goalId, startDate, endDate, user.getEmail());
        var result = scheduledTaskService.generateScheduleForFrequencyTask(goalId, actionItemId, startDate, endDate, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping
    @Operation(summary = "Get all scheduled tasks for a goal")
    public ResponseEntity<List<ScheduledTaskDto>> getScheduledTasks(
            @PathVariable Long goalId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal User user) {

        List<ScheduledTaskDto> result;
        if (startDate != null && endDate != null) {
            log.debug("Retrieving scheduled tasks for goal: {} from {} to {} by user: {}",
                    goalId, startDate, endDate, user.getEmail());
            result = scheduledTaskService.getScheduledTasksByDateRange(goalId, startDate, endDate, user);
        } else {
            log.debug("Retrieving all scheduled tasks for goal: {} by user: {}", goalId, user.getEmail());
            result = scheduledTaskService.getScheduledTasksByGoal(goalId, user);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/action-item/{actionItemId}")
    @Operation(summary = "Get scheduled tasks for a specific action item")
    public ResponseEntity<List<ScheduledTaskDto>> getScheduledTasksByActionItem(
            @PathVariable Long goalId,
            @PathVariable Long actionItemId,
            @AuthenticationPrincipal User user) {

        log.debug("Retrieving scheduled tasks for action item: {} in goal: {} by user: {}",
                actionItemId, goalId, user.getEmail());
        var result = scheduledTaskService.getScheduledTasksByActionItem(goalId, actionItemId, user);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending (overdue + today) scheduled tasks")
    public ResponseEntity<List<ScheduledTaskDto>> getPendingTasks(
            @PathVariable Long goalId,
            @AuthenticationPrincipal User user) {

        log.debug("Retrieving pending tasks for goal: {} by user: {}", goalId, user.getEmail());
        var result = scheduledTaskService.getPendingTasks(goalId, user);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{scheduledTaskId}/complete")
    @Operation(summary = "Mark a scheduled task as completed")
    public ResponseEntity<ScheduledTaskDto> markAsCompleted(
            @PathVariable Long goalId,
            @PathVariable Long scheduledTaskId,
            @AuthenticationPrincipal User user) {

        log.debug("Marking scheduled task: {} as completed in goal: {} by user: {}",
                scheduledTaskId, goalId, user.getEmail());
        var result = scheduledTaskService.markAsCompleted(goalId, scheduledTaskId, user);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{scheduledTaskId}/incomplete")
    @Operation(summary = "Mark a scheduled task as incomplete")
    public ResponseEntity<ScheduledTaskDto> markAsIncomplete(
            @PathVariable Long goalId,
            @PathVariable Long scheduledTaskId,
            @AuthenticationPrincipal User user) {

        log.debug("Marking scheduled task: {} as incomplete in goal: {} by user: {}",
                scheduledTaskId, goalId, user.getEmail());
        var result = scheduledTaskService.markAsIncomplete(goalId, scheduledTaskId, user);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{scheduledTaskId}")
    @Operation(summary = "Delete a scheduled task")
    public ResponseEntity<Void> deleteScheduledTask(
            @PathVariable Long goalId,
            @PathVariable Long scheduledTaskId,
            @AuthenticationPrincipal User user) {

        log.debug("Deleting scheduled task: {} from goal: {} by user: {}",
                scheduledTaskId, goalId, user.getEmail());
        scheduledTaskService.deleteScheduledTask(goalId, scheduledTaskId, user);
        return ResponseEntity.noContent().build();
    }
}
