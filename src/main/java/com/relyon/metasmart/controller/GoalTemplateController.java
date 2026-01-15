package com.relyon.metasmart.controller;

import com.relyon.metasmart.constant.ApiPaths;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.relyon.metasmart.entity.goal.GoalCategory;
import com.relyon.metasmart.entity.goal.dto.GoalRequest;
import com.relyon.metasmart.entity.template.dto.GoalTemplateRequest;
import com.relyon.metasmart.entity.template.dto.GoalTemplateResponse;
import com.relyon.metasmart.entity.template.dto.UpdateGoalTemplateRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.service.GoalTemplateService;
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
@RequestMapping(ApiPaths.GOAL_TEMPLATES)
@RequiredArgsConstructor
@Tag(name = "Goal Templates")
public class GoalTemplateController {

    private final GoalTemplateService goalTemplateService;

    @PostMapping
    public ResponseEntity<GoalTemplateResponse> create(
            @Valid @RequestBody GoalTemplateRequest request,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Received request to create goal template for user ID: {}", user.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(goalTemplateService.create(request, user));
    }

    @GetMapping
    public ResponseEntity<Page<GoalTemplateResponse>> findByOwner(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.debug("Received request to get goal templates for user ID: {}", user.getId());
        return ResponseEntity.ok(goalTemplateService.findByOwner(user, pageable));
    }

    @GetMapping("/available")
    public ResponseEntity<Page<GoalTemplateResponse>> findAvailable(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) GoalCategory category,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.debug("Received request to get available goal templates for user ID: {} with category: {}", user.getId(), category);
        return ResponseEntity.ok(goalTemplateService.findAvailable(user, category, pageable));
    }

    @GetMapping("/public")
    public ResponseEntity<Page<GoalTemplateResponse>> findPublic(
            @RequestParam(required = false) GoalCategory category,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.debug("Received request to get public goal templates with category: {}", category);
        return ResponseEntity.ok(goalTemplateService.findPublic(category, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalTemplateResponse> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Received request to get goal template ID: {} for user ID: {}", id, user.getId());
        return ResponseEntity.ok(goalTemplateService.findById(id, user));
    }

    @GetMapping("/{id}/goal")
    public ResponseEntity<GoalRequest> createGoalFromTemplate(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Received request to create goal from template ID: {} for user ID: {}", id, user.getId());
        return ResponseEntity.ok(goalTemplateService.createGoalFromTemplate(id, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalTemplateResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGoalTemplateRequest request,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Received request to update goal template ID: {} for user ID: {}", id, user.getId());
        return ResponseEntity.ok(goalTemplateService.update(id, request, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Received request to delete goal template ID: {} for user ID: {}", id, user.getId());
        goalTemplateService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
