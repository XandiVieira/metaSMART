package com.relyon.metasmart.controller;

import com.relyon.metasmart.constant.ApiPaths;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.relyon.metasmart.entity.obstacle.dto.ObstacleEntryRequest;
import com.relyon.metasmart.entity.obstacle.dto.ObstacleEntryResponse;
import com.relyon.metasmart.entity.obstacle.dto.UpdateObstacleEntryRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.service.ObstacleService;
import jakarta.validation.Valid;
import java.time.LocalDate;
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
@RequestMapping(ApiPaths.GOALS + "/{goalId}" + ApiPaths.OBSTACLES)
@RequiredArgsConstructor
@Tag(name = "Obstacles")
public class ObstacleController {

    private final ObstacleService obstacleService;

    @PostMapping
    public ResponseEntity<ObstacleEntryResponse> create(
            @PathVariable Long goalId,
            @Valid @RequestBody ObstacleEntryRequest request,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Received request to create obstacle entry for goal ID: {}", goalId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(obstacleService.create(goalId, request, user));
    }

    @GetMapping
    public ResponseEntity<Page<ObstacleEntryResponse>> findByGoal(
            @PathVariable Long goalId,
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "entryDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.debug("Received request to get obstacle entries for goal ID: {}", goalId);

        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(obstacleService.findByGoalAndDateRange(goalId, user, startDate, endDate, pageable));
        }
        return ResponseEntity.ok(obstacleService.findByGoal(goalId, user, pageable));
    }

    @PutMapping("/{entryId}")
    public ResponseEntity<ObstacleEntryResponse> update(
            @PathVariable Long goalId,
            @PathVariable Long entryId,
            @Valid @RequestBody UpdateObstacleEntryRequest request,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Received request to update obstacle entry ID: {} for goal ID: {}", entryId, goalId);
        return ResponseEntity.ok(obstacleService.update(goalId, entryId, request, user));
    }

    @DeleteMapping("/{entryId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long goalId,
            @PathVariable Long entryId,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Received request to delete obstacle entry ID: {} from goal ID: {}", entryId, goalId);
        obstacleService.delete(goalId, entryId, user);
        return ResponseEntity.noContent().build();
    }
}
