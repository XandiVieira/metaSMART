package com.relyon.metasmart.controller;

import com.relyon.metasmart.constant.ApiPaths;
import com.relyon.metasmart.entity.goal.GoalCategory;
import com.relyon.metasmart.entity.social.dto.CategoryStatsResponse;
import com.relyon.metasmart.entity.social.dto.GlobalStatsResponse;
import com.relyon.metasmart.entity.social.dto.GoalInsightsResponse;
import com.relyon.metasmart.entity.social.dto.MilestoneStatsResponse;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.service.SocialProofService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(ApiPaths.SOCIAL)
@RequiredArgsConstructor
@Tag(name = "Social Proof", description = "Anonymous aggregate statistics for motivation")
public class SocialProofController {

    private final SocialProofService socialProofService;

    @GetMapping("/stats")
    @Operation(summary = "Get global platform statistics")
    public ResponseEntity<GlobalStatsResponse> getGlobalStats() {
        log.debug("Getting global social proof stats");
        return ResponseEntity.ok(socialProofService.getGlobalStats());
    }

    @GetMapping("/stats/category/{category}")
    @Operation(summary = "Get statistics for a specific goal category")
    public ResponseEntity<CategoryStatsResponse> getCategoryStats(@PathVariable GoalCategory category) {
        log.debug("Getting social proof stats for category: {}", category);
        return ResponseEntity.ok(socialProofService.getCategoryStats(category));
    }

    @GetMapping("/goals/{goalId}/insights")
    @Operation(summary = "Get insights for a specific goal based on similar goals")
    public ResponseEntity<GoalInsightsResponse> getGoalInsights(
            @PathVariable Long goalId,
            @AuthenticationPrincipal User user) {
        log.debug("Getting insights for goal: {}", goalId);
        return ResponseEntity.ok(socialProofService.getGoalInsights(goalId, user));
    }

    @GetMapping("/goals/{goalId}/milestone-stats")
    @Operation(summary = "Get milestone statistics comparing to other users")
    public ResponseEntity<MilestoneStatsResponse> getMilestoneStats(
            @PathVariable Long goalId,
            @AuthenticationPrincipal User user) {
        log.debug("Getting milestone stats for goal: {}", goalId);
        return ResponseEntity.ok(socialProofService.getMilestoneStats(goalId, user));
    }
}
