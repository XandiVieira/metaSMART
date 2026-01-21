package com.relyon.metasmart.controller;

import com.relyon.metasmart.constant.ApiPaths;
import com.relyon.metasmart.entity.history.dto.ActivityHistoryResponse;
import com.relyon.metasmart.entity.history.dto.DailyActivityResponse;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.service.ActivityHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(ApiPaths.HISTORY)
@RequiredArgsConstructor
@Tag(name = "Activity History")
public class ActivityHistoryController {

    private final ActivityHistoryService activityHistoryService;

    @GetMapping
    @Operation(summary = "Get activity history grouped by day")
    public ResponseEntity<ActivityHistoryResponse> getActivityHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal User user) {
        log.debug("Get activity history request from {} to {} for user: {}", startDate, endDate, user.getEmail());
        return ResponseEntity.ok(activityHistoryService.getActivityHistory(user, startDate, endDate));
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "Get activity for a specific day")
    public ResponseEntity<DailyActivityResponse> getDailyActivity(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal User user) {
        log.debug("Get daily activity request for date: {} for user: {}", date, user.getEmail());
        return ResponseEntity.ok(activityHistoryService.getDailyActivity(user, date));
    }
}
