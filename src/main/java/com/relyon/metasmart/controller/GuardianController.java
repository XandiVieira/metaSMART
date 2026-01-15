package com.relyon.metasmart.controller;

import com.relyon.metasmart.constant.ApiPaths;
import com.relyon.metasmart.entity.guardian.dto.GoalGuardianResponse;
import com.relyon.metasmart.entity.guardian.dto.GuardedGoalResponse;
import com.relyon.metasmart.entity.guardian.dto.NudgeResponse;
import com.relyon.metasmart.entity.guardian.dto.SendNudgeRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.service.GoalGuardianService;
import com.relyon.metasmart.service.GuardianNudgeService;
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
@RequestMapping(ApiPaths.GUARDIAN)
@RequiredArgsConstructor
@Tag(name = "Guardian")
public class GuardianController {

    private final GoalGuardianService goalGuardianService;
    private final GuardianNudgeService guardianNudgeService;

    @GetMapping("/invitations")
    public ResponseEntity<Page<GoalGuardianResponse>> getPendingInvitations(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.debug("Getting pending invitations for user ID: {}", user.getId());
        return ResponseEntity.ok(goalGuardianService.getPendingInvitations(user, pageable));
    }

    @PutMapping("/invitations/{invitationId}/accept")
    public ResponseEntity<GoalGuardianResponse> acceptInvitation(
            @PathVariable Long invitationId,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Accepting invitation {} by user ID: {}", invitationId, user.getId());
        return ResponseEntity.ok(goalGuardianService.acceptInvitation(invitationId, user));
    }

    @PutMapping("/invitations/{invitationId}/decline")
    public ResponseEntity<GoalGuardianResponse> declineInvitation(
            @PathVariable Long invitationId,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Declining invitation {} by user ID: {}", invitationId, user.getId());
        return ResponseEntity.ok(goalGuardianService.declineInvitation(invitationId, user));
    }

    @GetMapping("/goals")
    public ResponseEntity<Page<GoalGuardianResponse>> getGuardedGoals(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.debug("Getting guarded goals for user ID: {}", user.getId());
        return ResponseEntity.ok(goalGuardianService.getGuardedGoals(user, pageable));
    }

    @GetMapping("/goals/{goalId}")
    public ResponseEntity<GuardedGoalResponse> getGuardedGoalDetails(
            @PathVariable Long goalId,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Getting guarded goal details for goal ID: {} by user ID: {}", goalId, user.getId());
        return ResponseEntity.ok(goalGuardianService.getGuardedGoalDetails(goalId, user));
    }

    @PostMapping("/goals/{goalId}/nudges")
    public ResponseEntity<NudgeResponse> sendNudge(
            @PathVariable Long goalId,
            @Valid @RequestBody SendNudgeRequest request,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Sending nudge to goal ID: {} by user ID: {}", goalId, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(guardianNudgeService.sendNudge(goalId, request, user));
    }

    @GetMapping("/nudges/sent")
    public ResponseEntity<Page<NudgeResponse>> getSentNudges(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.debug("Getting sent nudges for user ID: {}", user.getId());
        return ResponseEntity.ok(guardianNudgeService.getSentNudges(user, pageable));
    }

    @GetMapping("/nudges/unread-count")
    public ResponseEntity<Long> getUnreadNudgesCount(
            @AuthenticationPrincipal User user
    ) {
        log.debug("Getting unread nudges count for user ID: {}", user.getId());
        return ResponseEntity.ok(guardianNudgeService.countUnreadNudges(user));
    }
}
