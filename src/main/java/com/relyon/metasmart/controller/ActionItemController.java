package com.relyon.metasmart.controller;

import com.relyon.metasmart.constant.ApiPaths;
import com.relyon.metasmart.entity.actionplan.dto.ActionItemRequest;
import com.relyon.metasmart.entity.actionplan.dto.ActionItemResponse;
import com.relyon.metasmart.entity.actionplan.dto.UpdateActionItemRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.service.ActionItemService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(ApiPaths.GOALS + "/{goalId}" + ApiPaths.ACTION_ITEMS)
@RequiredArgsConstructor
@Tag(name = "Action Items")
public class ActionItemController {

    private final ActionItemService actionItemService;

    @PostMapping
    public ResponseEntity<ActionItemResponse> create(
            @PathVariable Long goalId,
            @Valid @RequestBody ActionItemRequest request,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Received request to create action item for goal ID: {}", goalId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(actionItemService.create(goalId, request, user));
    }

    @GetMapping
    public ResponseEntity<List<ActionItemResponse>> findByGoal(
            @PathVariable Long goalId,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Received request to get action items for goal ID: {}", goalId);
        return ResponseEntity.ok(actionItemService.findByGoal(goalId, user));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ActionItemResponse> findById(
            @PathVariable Long goalId,
            @PathVariable Long itemId,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Received request to get action item ID: {} for goal ID: {}", itemId, goalId);
        return ResponseEntity.ok(actionItemService.findById(goalId, itemId, user));
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<ActionItemResponse> update(
            @PathVariable Long goalId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateActionItemRequest request,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Received request to update action item ID: {} for goal ID: {}", itemId, goalId);
        return ResponseEntity.ok(actionItemService.update(goalId, itemId, request, user));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long goalId,
            @PathVariable Long itemId,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Received request to delete action item ID: {} from goal ID: {}", itemId, goalId);
        actionItemService.delete(goalId, itemId, user);
        return ResponseEntity.noContent().build();
    }
}
