package com.relyon.metasmart.controller;

import com.relyon.metasmart.constant.ApiPaths;
import com.relyon.metasmart.entity.reflection.dto.PendingReflectionResponse;
import com.relyon.metasmart.entity.reflection.dto.ReflectionRequest;
import com.relyon.metasmart.entity.reflection.dto.ReflectionResponse;
import com.relyon.metasmart.entity.reflection.dto.ReflectionStatusResponse;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.service.ReflectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(ApiPaths.GOALS)
@RequiredArgsConstructor
@Tag(name = "Reflections")
public class ReflectionController {

    private final ReflectionService reflectionService;

    @GetMapping("/reflections/pending")
    @Operation(summary = "Get all pending reflections across all goals")
    public ResponseEntity<List<PendingReflectionResponse>> getPendingReflections(
            @AuthenticationPrincipal User user) {
        log.debug("Getting pending reflections for user: {}", user.getEmail());
        return ResponseEntity.ok(reflectionService.getPendingReflections(user));
    }

    @GetMapping("/{goalId}/reflections/status")
    @Operation(summary = "Get reflection status for a goal")
    public ResponseEntity<ReflectionStatusResponse> getReflectionStatus(
            @PathVariable Long goalId,
            @AuthenticationPrincipal User user) {
        log.debug("Getting reflection status for goal: {} by user: {}", goalId, user.getEmail());
        return ResponseEntity.ok(reflectionService.getReflectionStatus(goalId, user));
    }

    @PostMapping("/{goalId}/reflections")
    @Operation(summary = "Create a reflection for the current period")
    public ResponseEntity<ReflectionResponse> createReflection(
            @PathVariable Long goalId,
            @Valid @RequestBody ReflectionRequest request,
            @AuthenticationPrincipal User user) {
        log.debug("Creating reflection for goal: {} by user: {}", goalId, user.getEmail());
        return ResponseEntity.ok(reflectionService.createReflection(goalId, request, user));
    }

    @GetMapping("/{goalId}/reflections")
    @Operation(summary = "Get reflection history for a goal")
    public ResponseEntity<Page<ReflectionResponse>> getReflectionHistory(
            @PathVariable Long goalId,
            @AuthenticationPrincipal User user,
            Pageable pageable) {
        log.debug("Getting reflection history for goal: {} by user: {}", goalId, user.getEmail());
        return ResponseEntity.ok(reflectionService.getReflectionHistory(goalId, user, pageable));
    }

    @GetMapping("/{goalId}/reflections/{reflectionId}")
    @Operation(summary = "Get a specific reflection")
    public ResponseEntity<ReflectionResponse> getReflection(
            @PathVariable Long goalId,
            @PathVariable Long reflectionId,
            @AuthenticationPrincipal User user) {
        log.debug("Getting reflection: {} for goal: {}", reflectionId, goalId);
        return ResponseEntity.ok(reflectionService.getReflection(goalId, reflectionId, user));
    }

    @PutMapping("/{goalId}/reflections/{reflectionId}")
    @Operation(summary = "Update a reflection")
    public ResponseEntity<ReflectionResponse> updateReflection(
            @PathVariable Long goalId,
            @PathVariable Long reflectionId,
            @Valid @RequestBody ReflectionRequest request,
            @AuthenticationPrincipal User user) {
        log.debug("Updating reflection: {} for goal: {}", reflectionId, goalId);
        return ResponseEntity.ok(reflectionService.updateReflection(goalId, reflectionId, request, user));
    }
}
