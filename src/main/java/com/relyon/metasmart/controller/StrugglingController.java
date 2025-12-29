package com.relyon.metasmart.controller;

import com.relyon.metasmart.constant.ApiPaths;
import com.relyon.metasmart.entity.struggling.dto.StrugglingHelpRequest;
import com.relyon.metasmart.entity.struggling.dto.StrugglingHelpResponse;
import com.relyon.metasmart.entity.struggling.dto.StrugglingStatusResponse;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.service.StrugglingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "Struggling Help")
public class StrugglingController {

    private final StrugglingService strugglingService;

    @GetMapping("/struggling/status")
    @Operation(summary = "Get struggling help status (free requests remaining)")
    public ResponseEntity<StrugglingStatusResponse> getStatus(@AuthenticationPrincipal User user) {
        log.debug("Getting struggling status for user: {}", user.getEmail());
        return ResponseEntity.ok(strugglingService.getStatus(user));
    }

    @PostMapping("/{goalId}/struggling")
    @Operation(summary = "Request help when struggling with a goal")
    public ResponseEntity<StrugglingHelpResponse> requestHelp(
            @PathVariable Long goalId,
            @Valid @RequestBody StrugglingHelpRequest request,
            @AuthenticationPrincipal User user) {
        log.debug("User {} requesting help for goal {}", user.getEmail(), goalId);
        return ResponseEntity.ok(strugglingService.requestHelp(goalId, request, user));
    }

    @GetMapping("/struggling/history")
    @Operation(summary = "Get history of struggling help requests")
    public ResponseEntity<Page<StrugglingHelpResponse>> getHistory(
            @AuthenticationPrincipal User user,
            Pageable pageable) {
        log.debug("Getting struggling history for user: {}", user.getEmail());
        return ResponseEntity.ok(strugglingService.getHistory(user, pageable));
    }

    @PutMapping("/struggling/{requestId}/feedback")
    @Operation(summary = "Mark if the help was useful")
    public ResponseEntity<Void> markHelpful(
            @PathVariable Long requestId,
            @RequestParam Boolean wasHelpful,
            @AuthenticationPrincipal User user) {
        log.debug("Marking struggling request {} as helpful: {}", requestId, wasHelpful);
        strugglingService.markHelpful(requestId, wasHelpful, user);
        return ResponseEntity.ok().build();
    }
}
