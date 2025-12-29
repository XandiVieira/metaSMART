package com.relyon.metasmart.controller;

import com.relyon.metasmart.constant.ApiPaths;
import com.relyon.metasmart.entity.dashboard.dto.DashboardResponse;
import com.relyon.metasmart.entity.dashboard.dto.GoalStatsResponse;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(ApiPaths.DASHBOARD)
@RequiredArgsConstructor
@Tag(name = "Dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @Operation(summary = "Get dashboard summary")
    public ResponseEntity<DashboardResponse> getDashboard(@AuthenticationPrincipal User user) {
        log.debug("Getting dashboard for user: {}", user.getEmail());
        return ResponseEntity.ok(dashboardService.getDashboard(user));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get goal statistics")
    public ResponseEntity<GoalStatsResponse> getGoalStats(@AuthenticationPrincipal User user) {
        log.debug("Getting goal stats for user: {}", user.getEmail());
        return ResponseEntity.ok(dashboardService.getGoalStats(user));
    }
}
