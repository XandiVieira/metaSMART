package com.relyon.metasmart.controller;

import com.relyon.metasmart.constant.ApiPaths;
import com.relyon.metasmart.entity.goal.GoalCategory;
import com.relyon.metasmart.entity.goal.GoalStatus;
import com.relyon.metasmart.entity.goal.dto.GoalRequest;
import com.relyon.metasmart.entity.goal.dto.GoalResponse;
import com.relyon.metasmart.entity.goal.dto.UpdateGoalRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.service.GoalService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(ApiPaths.GOALS)
@RequiredArgsConstructor
@Tag(name = "Goals")
public class GoalController {

    private final GoalService goalService;

    @PostMapping
    public ResponseEntity<GoalResponse> create(
            @Valid @RequestBody GoalRequest request,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Received request to create goal for user ID: {}", user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(goalService.create(request, user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Received request to get goal ID: {} for user ID: {}", id, user.getId());
        return ResponseEntity.ok(goalService.findById(id, user));
    }

    @GetMapping
    public ResponseEntity<Page<GoalResponse>> findAll(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.debug("Received request to get all goals for user ID: {}", user.getId());
        return ResponseEntity.ok(goalService.findAll(user, pageable));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<GoalResponse>> findByStatus(
            @PathVariable("status") GoalStatus goalStatus,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.debug("Received request to get goals by status: {} for user ID: {}", goalStatus, user.getId());
        return ResponseEntity.ok(goalService.findByStatus(user, goalStatus, pageable));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Page<GoalResponse>> findByCategory(
            @PathVariable("category") GoalCategory goalCategory,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.debug("Received request to get goals by category: {} for user ID: {}", goalCategory, user.getId());
        return ResponseEntity.ok(goalService.findByCategory(user, goalCategory, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGoalRequest request,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Received request to update goal ID: {} for user ID: {}", id, user.getId());
        return ResponseEntity.ok(goalService.update(id, request, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Received request to delete goal ID: {} for user ID: {}", id, user.getId());
        goalService.delete(id, user);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<GoalResponse> archive(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Received request to archive goal ID: {} for user ID: {}", id, user.getId());
        return ResponseEntity.ok(goalService.archive(id, user));
    }

    @PutMapping("/{id}/unarchive")
    public ResponseEntity<GoalResponse> unarchive(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Received request to unarchive goal ID: {} for user ID: {}", id, user.getId());
        return ResponseEntity.ok(goalService.unarchive(id, user));
    }

    @GetMapping("/archived")
    public ResponseEntity<Page<GoalResponse>> findArchived(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10, sort = "archivedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.debug("Received request to get archived goals for user ID: {}", user.getId());
        return ResponseEntity.ok(goalService.findArchived(user, pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<GoalResponse>> search(
            @RequestParam String query,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.debug("Searching goals with query: {} for user ID: {}", query, user.getId());
        return ResponseEntity.ok(goalService.search(user, query, pageable));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<GoalResponse>> filter(
            @RequestParam(required = false) GoalStatus status,
            @RequestParam(required = false) GoalCategory category,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.debug("Filtering goals with status: {}, category: {} for user ID: {}", status, category, user.getId());
        return ResponseEntity.ok(goalService.filter(user, status, category, pageable));
    }

    @GetMapping("/due-soon")
    public ResponseEntity<java.util.List<GoalResponse>> dueSoon(
            @RequestParam(defaultValue = "7") int days,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Finding goals due within {} days for user ID: {}", days, user.getId());
        return ResponseEntity.ok(goalService.findDueSoon(user, days));
    }

    @PostMapping("/{id}/duplicate")
    public ResponseEntity<GoalResponse> duplicate(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Received request to duplicate goal ID: {} for user ID: {}", id, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(goalService.duplicate(id, user));
    }

    @PostMapping("/{id}/use-streak-shield")
    public ResponseEntity<GoalResponse> useStreakShield(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Received request to use streak shield for goal ID: {} for user ID: {}", id, user.getId());
        return ResponseEntity.ok(goalService.useStreakShield(id, user));
    }
}
