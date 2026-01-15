package com.relyon.metasmart.controller;

import com.relyon.metasmart.constant.ApiPaths;
import com.relyon.metasmart.entity.progress.dto.*;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.service.ProgressService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(ApiPaths.GOALS + "/{goalId}")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @PostMapping(ApiPaths.PROGRESS)
    public ResponseEntity<ProgressEntryResponse> addProgress(
            @PathVariable Long goalId,
            @Valid @RequestBody ProgressEntryRequest request,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Received request to add progress for goal ID: {}", goalId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(progressService.addProgress(goalId, request, user));
    }

    @PostMapping(ApiPaths.PROGRESS + "/bulk")
    public ResponseEntity<List<ProgressEntryResponse>> addBulkProgress(
            @PathVariable Long goalId,
            @Valid @RequestBody BulkProgressRequest request,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Received request to add {} bulk progress entries for goal ID: {}", request.getEntries().size(), goalId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(progressService.addBulkProgress(goalId, request, user));
    }

    @GetMapping(ApiPaths.PROGRESS)
    public ResponseEntity<Page<ProgressEntryResponse>> getProgressHistory(
            @PathVariable Long goalId,
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.debug("Received request to get progress history for goal ID: {}", goalId);

        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(progressService.getProgressHistoryByDateRange(goalId, user, startDate, endDate, pageable));
        }
        return ResponseEntity.ok(progressService.getProgressHistory(goalId, user, pageable));
    }

    @PutMapping(ApiPaths.PROGRESS + "/{entryId}")
    public ResponseEntity<ProgressEntryResponse> updateProgressEntry(
            @PathVariable Long goalId,
            @PathVariable Long entryId,
            @Valid @RequestBody UpdateProgressEntryRequest request,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Received request to update progress entry ID: {} for goal ID: {}", entryId, goalId);
        return ResponseEntity.ok(progressService.updateProgressEntry(goalId, entryId, request, user));
    }

    @DeleteMapping(ApiPaths.PROGRESS + "/{entryId}")
    public ResponseEntity<Void> deleteProgressEntry(
            @PathVariable Long goalId,
            @PathVariable Long entryId,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Received request to delete progress entry ID: {} from goal ID: {}", entryId, goalId);
        progressService.deleteProgressEntry(goalId, entryId, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(ApiPaths.MILESTONES)
    public ResponseEntity<MilestoneResponse> addMilestone(
            @PathVariable Long goalId,
            @Valid @RequestBody MilestoneRequest request,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Received request to add milestone for goal ID: {}", goalId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(progressService.addMilestone(goalId, request, user));
    }

    @GetMapping(ApiPaths.MILESTONES)
    public ResponseEntity<List<MilestoneResponse>> getMilestones(
            @PathVariable Long goalId,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Received request to get milestones for goal ID: {}", goalId);
        return ResponseEntity.ok(progressService.getMilestones(goalId, user));
    }

    @DeleteMapping(ApiPaths.MILESTONES + "/{milestoneId}")
    public ResponseEntity<Void> deleteMilestone(
            @PathVariable Long goalId,
            @PathVariable Long milestoneId,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Received request to delete milestone ID: {} from goal ID: {}", milestoneId, goalId);
        progressService.deleteMilestone(goalId, milestoneId, user);
        return ResponseEntity.noContent().build();
    }
}
