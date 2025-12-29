package com.relyon.metasmart.service;

import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.goal.GoalNote;
import com.relyon.metasmart.entity.goal.dto.GoalNoteRequest;
import com.relyon.metasmart.entity.goal.dto.GoalNoteResponse;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.repository.GoalNoteRepository;
import com.relyon.metasmart.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoalNoteService {

    private final GoalNoteRepository goalNoteRepository;
    private final GoalRepository goalRepository;

    @Transactional(readOnly = true)
    public Page<GoalNoteResponse> getNotes(Long goalId, User owner, GoalNote.NoteType noteType, Pageable pageable) {
        log.debug("Getting notes for goal {} by user {}", goalId, owner.getEmail());

        var goal = findGoalByIdAndOwner(goalId, owner);

        Page<GoalNote> notes;
        if (noteType != null) {
            notes = goalNoteRepository.findByGoalAndNoteTypeOrderByCreatedAtDesc(goal, noteType, pageable);
        } else {
            notes = goalNoteRepository.findByGoalOrderByCreatedAtDesc(goal, pageable);
        }

        return notes.map(this::mapToResponse);
    }

    @Transactional
    public GoalNoteResponse createNote(Long goalId, User owner, GoalNoteRequest request) {
        log.debug("Creating note for goal {} by user {}", goalId, owner.getEmail());

        var goal = findGoalByIdAndOwner(goalId, owner);

        var note = GoalNote.builder()
                .goal(goal)
                .content(request.getContent())
                .noteType(request.getNoteType() != null ? request.getNoteType() : GoalNote.NoteType.GENERAL)
                .build();

        var savedNote = goalNoteRepository.save(note);
        log.info("Note created for goal {}: {}", goalId, savedNote.getId());

        return mapToResponse(savedNote);
    }

    @Transactional
    public GoalNoteResponse updateNote(Long goalId, Long noteId, User owner, GoalNoteRequest request) {
        log.debug("Updating note {} for goal {} by user {}", noteId, goalId, owner.getEmail());

        var goal = findGoalByIdAndOwner(goalId, owner);
        var note = goalNoteRepository.findByIdAndGoal(noteId, goal)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + noteId));

        Optional.ofNullable(request.getContent()).ifPresent(note::setContent);
        Optional.ofNullable(request.getNoteType()).ifPresent(note::setNoteType);

        var savedNote = goalNoteRepository.save(note);
        log.info("Note updated: {}", savedNote.getId());

        return mapToResponse(savedNote);
    }

    @Transactional
    public void deleteNote(Long goalId, Long noteId, User owner) {
        log.debug("Deleting note {} for goal {} by user {}", noteId, goalId, owner.getEmail());

        var goal = findGoalByIdAndOwner(goalId, owner);
        var note = goalNoteRepository.findByIdAndGoal(noteId, goal)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + noteId));

        goalNoteRepository.delete(note);
        log.info("Note deleted: {}", noteId);
    }

    private Goal findGoalByIdAndOwner(Long goalId, User owner) {
        return goalRepository.findByIdAndOwnerAndArchivedAtIsNull(goalId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + goalId));
    }

    private GoalNoteResponse mapToResponse(GoalNote note) {
        return GoalNoteResponse.builder()
                .id(note.getId())
                .goalId(note.getGoal().getId())
                .content(note.getContent())
                .noteType(note.getNoteType())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}
