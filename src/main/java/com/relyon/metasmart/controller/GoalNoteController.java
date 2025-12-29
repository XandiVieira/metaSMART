package com.relyon.metasmart.controller;

import com.relyon.metasmart.constant.ApiPaths;
import com.relyon.metasmart.entity.goal.GoalNote;
import com.relyon.metasmart.entity.goal.dto.GoalNoteRequest;
import com.relyon.metasmart.entity.goal.dto.GoalNoteResponse;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.service.GoalNoteService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping(ApiPaths.GOALS + "/{goalId}/notes")
@RequiredArgsConstructor
@Tag(name = "Goal Notes")
public class GoalNoteController {

    private final GoalNoteService goalNoteService;

    @GetMapping
    @Operation(summary = "Get all notes for a goal")
    public ResponseEntity<Page<GoalNoteResponse>> getNotes(
            @PathVariable Long goalId,
            @RequestParam(required = false) GoalNote.NoteType noteType,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("Getting notes for goal {} by user {}", goalId, user.getEmail());
        return ResponseEntity.ok(goalNoteService.getNotes(goalId, user, noteType, pageable));
    }

    @PostMapping
    @Operation(summary = "Create a new note for a goal")
    public ResponseEntity<GoalNoteResponse> createNote(
            @PathVariable Long goalId,
            @Valid @RequestBody GoalNoteRequest request,
            @AuthenticationPrincipal User user) {
        log.debug("Creating note for goal {} by user {}", goalId, user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(goalNoteService.createNote(goalId, user, request));
    }

    @PutMapping("/{noteId}")
    @Operation(summary = "Update a note")
    public ResponseEntity<GoalNoteResponse> updateNote(
            @PathVariable Long goalId,
            @PathVariable Long noteId,
            @Valid @RequestBody GoalNoteRequest request,
            @AuthenticationPrincipal User user) {
        log.debug("Updating note {} for goal {} by user {}", noteId, goalId, user.getEmail());
        return ResponseEntity.ok(goalNoteService.updateNote(goalId, noteId, user, request));
    }

    @DeleteMapping("/{noteId}")
    @Operation(summary = "Delete a note")
    public ResponseEntity<Void> deleteNote(
            @PathVariable Long goalId,
            @PathVariable Long noteId,
            @AuthenticationPrincipal User user) {
        log.debug("Deleting note {} for goal {} by user {}", noteId, goalId, user.getEmail());
        goalNoteService.deleteNote(goalId, noteId, user);
        return ResponseEntity.noContent().build();
    }
}
