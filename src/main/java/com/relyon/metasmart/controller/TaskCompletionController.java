package com.relyon.metasmart.controller;

import com.relyon.metasmart.constant.ApiPaths;
import com.relyon.metasmart.entity.actionplan.dto.TaskCompletionDto;
import com.relyon.metasmart.entity.actionplan.dto.TaskCompletionRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.service.TaskCompletionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(ApiPaths.GOALS + "/{goalId}" + ApiPaths.ACTION_ITEMS + "/{actionItemId}" + ApiPaths.COMPLETIONS)
@RequiredArgsConstructor
@Tag(name = "Task Completions", description = "Track completions for recurring tasks")
public class TaskCompletionController {

    private final TaskCompletionService taskCompletionService;

    @PostMapping
    @Operation(summary = "Record a task completion")
    public ResponseEntity<TaskCompletionDto> recordCompletion(
            @PathVariable Long goalId,
            @PathVariable Long actionItemId,
            @Valid @RequestBody(required = false) TaskCompletionRequest request,
            @AuthenticationPrincipal User user) {

        TaskCompletionDto result;
        if (request != null && request.getDate() != null) {
            result = taskCompletionService.recordCompletionForDate(goalId, actionItemId, request.getDate(),
                    request.getNote(), user);
        } else {
            result = taskCompletionService.recordCompletion(goalId, actionItemId,
                    request != null ? request.getNote() : null, user);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping
    @Operation(summary = "Get completion history for a task")
    public ResponseEntity<List<TaskCompletionDto>> getCompletionHistory(
            @PathVariable Long goalId,
            @PathVariable Long actionItemId,
            @AuthenticationPrincipal User user) {

        var history = taskCompletionService.getCompletionHistory(goalId, actionItemId, user);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/paginated")
    @Operation(summary = "Get paginated completion history")
    public ResponseEntity<Page<TaskCompletionDto>> getCompletionHistoryPaginated(
            @PathVariable Long goalId,
            @PathVariable Long actionItemId,
            @AuthenticationPrincipal User user,
            Pageable pageable) {

        var history = taskCompletionService.getCompletionHistoryPaginated(goalId, actionItemId, user, pageable);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/range")
    @Operation(summary = "Get completions within a date range")
    public ResponseEntity<List<TaskCompletionDto>> getCompletionsByDateRange(
            @PathVariable Long goalId,
            @PathVariable Long actionItemId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal User user) {

        var completions = taskCompletionService.getCompletionsByDateRange(goalId, actionItemId, startDate, endDate, user);
        return ResponseEntity.ok(completions);
    }

    @GetMapping("/count")
    @Operation(summary = "Count total completions for a task")
    public ResponseEntity<Long> countCompletions(
            @PathVariable Long goalId,
            @PathVariable Long actionItemId,
            @AuthenticationPrincipal User user) {

        var count = taskCompletionService.countCompletions(goalId, actionItemId, user);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count/range")
    @Operation(summary = "Count completions within a date range")
    public ResponseEntity<Long> countCompletionsInPeriod(
            @PathVariable Long goalId,
            @PathVariable Long actionItemId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal User user) {

        var count = taskCompletionService.countCompletionsInPeriod(goalId, actionItemId, startDate, endDate, user);
        return ResponseEntity.ok(count);
    }

    @DeleteMapping("/{completionId}")
    @Operation(summary = "Delete a completion record")
    public ResponseEntity<Void> deleteCompletion(
            @PathVariable Long goalId,
            @PathVariable Long actionItemId,
            @PathVariable Long completionId,
            @AuthenticationPrincipal User user) {

        taskCompletionService.deleteCompletion(goalId, actionItemId, completionId, user);
        return ResponseEntity.noContent().build();
    }
}
