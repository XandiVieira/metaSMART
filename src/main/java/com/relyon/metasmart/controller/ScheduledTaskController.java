package com.relyon.metasmart.controller;

import com.relyon.metasmart.constant.ApiPaths;
import com.relyon.metasmart.entity.actionplan.dto.ScheduledTaskDto;
import com.relyon.metasmart.entity.actionplan.dto.ScheduledTaskRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.service.ScheduledTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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
            result = scheduledTaskService.getScheduledTasksByDateRange(goalId, startDate, endDate, user);
        } else {
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

        var result = scheduledTaskService.getScheduledTasksByActionItem(goalId, actionItemId, user);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending (overdue + today) scheduled tasks")
    public ResponseEntity<List<ScheduledTaskDto>> getPendingTasks(
            @PathVariable Long goalId,
            @AuthenticationPrincipal User user) {

        var result = scheduledTaskService.getPendingTasks(goalId, user);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{scheduledTaskId}/complete")
    @Operation(summary = "Mark a scheduled task as completed")
    public ResponseEntity<ScheduledTaskDto> markAsCompleted(
            @PathVariable Long goalId,
            @PathVariable Long scheduledTaskId,
            @AuthenticationPrincipal User user) {

        var result = scheduledTaskService.markAsCompleted(goalId, scheduledTaskId, user);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{scheduledTaskId}/incomplete")
    @Operation(summary = "Mark a scheduled task as incomplete")
    public ResponseEntity<ScheduledTaskDto> markAsIncomplete(
            @PathVariable Long goalId,
            @PathVariable Long scheduledTaskId,
            @AuthenticationPrincipal User user) {

        var result = scheduledTaskService.markAsIncomplete(goalId, scheduledTaskId, user);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{scheduledTaskId}")
    @Operation(summary = "Delete a scheduled task")
    public ResponseEntity<Void> deleteScheduledTask(
            @PathVariable Long goalId,
            @PathVariable Long scheduledTaskId,
            @AuthenticationPrincipal User user) {

        scheduledTaskService.deleteScheduledTask(goalId, scheduledTaskId, user);
        return ResponseEntity.noContent().build();
    }
}
