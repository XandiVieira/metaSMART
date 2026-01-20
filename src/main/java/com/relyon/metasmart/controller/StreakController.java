package com.relyon.metasmart.controller;

import com.relyon.metasmart.constant.ApiPaths;
import com.relyon.metasmart.entity.streak.dto.StreakResponse;
import com.relyon.metasmart.entity.streak.dto.StreakSummaryResponse;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.service.StreakService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(ApiPaths.STREAKS)
@RequiredArgsConstructor
@Tag(name = "Streaks", description = "Streak tracking endpoints")
public class StreakController {

    private final StreakService streakService;

    @GetMapping("/summary")
    @Operation(summary = "Get user's streak summary")
    public ResponseEntity<StreakSummaryResponse> getStreakSummary(
            @AuthenticationPrincipal User user) {

        log.debug("Getting streak summary for user: {}", user.getEmail());
        var summary = streakService.getUserStreakSummary(user);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/user")
    @Operation(summary = "Get user-level streak")
    public ResponseEntity<StreakResponse> getUserStreak(
            @AuthenticationPrincipal User user) {

        log.debug("Getting user-level streak for user: {}", user.getEmail());
        var streak = streakService.getUserStreak(user);
        return ResponseEntity.ok(streak);
    }

    @GetMapping("/goals/{goalId}")
    @Operation(summary = "Get goal-level streak")
    public ResponseEntity<StreakResponse> getGoalStreak(
            @PathVariable Long goalId,
            @AuthenticationPrincipal User user) {

        log.debug("Getting streak for goal: {} by user: {}", goalId, user.getEmail());
        var streak = streakService.getGoalStreak(goalId, user);
        return ResponseEntity.ok(streak);
    }

    @GetMapping("/goals/{goalId}/tasks/{actionItemId}")
    @Operation(summary = "Get task-level streak")
    public ResponseEntity<StreakResponse> getTaskStreak(
            @PathVariable Long goalId,
            @PathVariable Long actionItemId,
            @AuthenticationPrincipal User user) {

        log.debug("Getting streak for task: {} in goal: {} by user: {}", actionItemId, goalId, user.getEmail());
        var streak = streakService.getTaskStreak(goalId, actionItemId, user);
        return ResponseEntity.ok(streak);
    }

    @GetMapping("/goals/{goalId}/all")
    @Operation(summary = "Get all streaks for a goal (goal + tasks)")
    public ResponseEntity<List<StreakResponse>> getGoalStreaks(
            @PathVariable Long goalId,
            @AuthenticationPrincipal User user) {

        log.debug("Getting all streaks for goal: {} by user: {}", goalId, user.getEmail());
        var streaks = streakService.getGoalStreaks(goalId, user);
        return ResponseEntity.ok(streaks);
    }
}
