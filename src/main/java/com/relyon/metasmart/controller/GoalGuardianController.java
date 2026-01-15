package com.relyon.metasmart.controller;

import com.relyon.metasmart.constant.ApiPaths;
import com.relyon.metasmart.entity.guardian.dto.GoalGuardianResponse;
import com.relyon.metasmart.entity.guardian.dto.InviteGuardianRequest;
import com.relyon.metasmart.entity.guardian.dto.NudgeResponse;
import com.relyon.metasmart.entity.guardian.dto.ReactToNudgeRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.service.GoalGuardianService;
import com.relyon.metasmart.service.GuardianNudgeService;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping(ApiPaths.GOALS + "/{goalId}" + ApiPaths.GUARDIANS)
@RequiredArgsConstructor
public class GoalGuardianController {

    private final GoalGuardianService goalGuardianService;
    private final GuardianNudgeService guardianNudgeService;

    @PostMapping
    public ResponseEntity<GoalGuardianResponse> inviteGuardian(
            @PathVariable Long goalId,
            @Valid @RequestBody InviteGuardianRequest request,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Inviting guardian for goal ID: {} by user ID: {}", goalId, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(goalGuardianService.inviteGuardian(goalId, request, user));
    }

    @GetMapping
    public ResponseEntity<List<GoalGuardianResponse>> getGuardians(
            @PathVariable Long goalId,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Getting guardians for goal ID: {} by user ID: {}", goalId, user.getId());
        return ResponseEntity.ok(goalGuardianService.getGuardiansForGoal(goalId, user));
    }

    @DeleteMapping("/{guardianshipId}")
    public ResponseEntity<Void> removeGuardian(
            @PathVariable Long goalId,
            @PathVariable Long guardianshipId,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Removing guardian {} from goal ID: {} by user ID: {}", guardianshipId, goalId, user.getId());
        goalGuardianService.removeGuardian(goalId, guardianshipId, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(ApiPaths.NUDGES)
    public ResponseEntity<Page<NudgeResponse>> getNudges(
            @PathVariable Long goalId,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.debug("Getting nudges for goal ID: {} by user ID: {}", goalId, user.getId());
        return ResponseEntity.ok(guardianNudgeService.getNudgesForGoal(goalId, user, pageable));
    }

    @PutMapping(ApiPaths.NUDGES + "/{nudgeId}/read")
    public ResponseEntity<NudgeResponse> markNudgeAsRead(
            @PathVariable Long goalId,
            @PathVariable Long nudgeId,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Marking nudge {} as read for goal ID: {} by user ID: {}", nudgeId, goalId, user.getId());
        return ResponseEntity.ok(guardianNudgeService.markAsRead(goalId, nudgeId, user));
    }

    @PutMapping(ApiPaths.NUDGES + "/{nudgeId}/react")
    public ResponseEntity<NudgeResponse> reactToNudge(
            @PathVariable Long goalId,
            @PathVariable Long nudgeId,
            @Valid @RequestBody ReactToNudgeRequest request,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Reacting to nudge {} for goal ID: {} by user ID: {}", nudgeId, goalId, user.getId());
        return ResponseEntity.ok(guardianNudgeService.reactToNudge(goalId, nudgeId, request, user));
    }
}
