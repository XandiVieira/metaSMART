package com.relyon.metasmart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.goal.GoalCategory;
import com.relyon.metasmart.entity.goal.GoalNote;
import com.relyon.metasmart.entity.goal.GoalStatus;
import com.relyon.metasmart.entity.goal.dto.GoalNoteRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.repository.GoalNoteRepository;
import com.relyon.metasmart.repository.GoalRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class GoalNoteServiceTest {

    @Mock
    private GoalNoteRepository goalNoteRepository;

    @Mock
    private GoalRepository goalRepository;

    @InjectMocks
    private GoalNoteService goalNoteService;

    private User user;
    private Goal goal;
    private GoalNote note;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        goal = Goal.builder()
                .id(1L)
                .title("Run 5km")
                .description("Build endurance to run 5km")
                .goalCategory(GoalCategory.HEALTH)
                .targetValue("5")
                .unit("km")
                .currentProgress(BigDecimal.ZERO)
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusMonths(3))
                .goalStatus(GoalStatus.ACTIVE)
                .owner(user)
                .build();

        note = GoalNote.builder()
                .id(1L)
                .goal(goal)
                .content("Feeling great today!")
                .noteType(GoalNote.NoteType.GENERAL)
                .build();
    }

    @Nested
    @DisplayName("Get notes tests")
    class GetNotesTests {

        @Test
        @DisplayName("Should get all notes for a goal")
        void shouldGetAllNotesForGoal() {
            var pageable = Pageable.unpaged();
            var notes = new PageImpl<>(List.of(note));

            when(goalRepository.findByIdAndOwnerAndArchivedAtIsNull(1L, user)).thenReturn(Optional.of(goal));
            when(goalNoteRepository.findByGoalOrderByCreatedAtDesc(goal, pageable)).thenReturn(notes);

            var response = goalNoteService.getNotes(1L, user, null, pageable);

            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should filter notes by type")
        void shouldFilterNotesByType() {
            var pageable = Pageable.unpaged();
            var notes = new PageImpl<>(List.of(note));

            when(goalRepository.findByIdAndOwnerAndArchivedAtIsNull(1L, user)).thenReturn(Optional.of(goal));
            when(goalNoteRepository.findByGoalAndNoteTypeOrderByCreatedAtDesc(goal, GoalNote.NoteType.GENERAL, pageable))
                    .thenReturn(notes);

            var response = goalNoteService.getNotes(1L, user, GoalNote.NoteType.GENERAL, pageable);

            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should throw exception when goal not found")
        void shouldThrowExceptionWhenGoalNotFound() {
            when(goalRepository.findByIdAndOwnerAndArchivedAtIsNull(1L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> goalNoteService.getNotes(1L, user, null, Pageable.unpaged()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Create note tests")
    class CreateNoteTests {

        @Test
        @DisplayName("Should create note successfully")
        void shouldCreateNoteSuccessfully() {
            var request = GoalNoteRequest.builder()
                    .content("New note content")
                    .noteType(GoalNote.NoteType.REFLECTION)
                    .build();

            when(goalRepository.findByIdAndOwnerAndArchivedAtIsNull(1L, user)).thenReturn(Optional.of(goal));
            when(goalNoteRepository.save(any(GoalNote.class))).thenReturn(note);

            var response = goalNoteService.createNote(1L, user, request);

            assertThat(response).isNotNull();
            verify(goalNoteRepository).save(any(GoalNote.class));
        }
    }

    @Nested
    @DisplayName("Update note tests")
    class UpdateNoteTests {

        @Test
        @DisplayName("Should update note successfully")
        void shouldUpdateNoteSuccessfully() {
            var request = GoalNoteRequest.builder()
                    .content("Updated content")
                    .noteType(GoalNote.NoteType.MILESTONE)
                    .build();

            when(goalRepository.findByIdAndOwnerAndArchivedAtIsNull(1L, user)).thenReturn(Optional.of(goal));
            when(goalNoteRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.of(note));
            when(goalNoteRepository.save(any(GoalNote.class))).thenReturn(note);

            var response = goalNoteService.updateNote(1L, 1L, user, request);

            assertThat(response).isNotNull();
            verify(goalNoteRepository).save(any(GoalNote.class));
        }

        @Test
        @DisplayName("Should throw exception when note not found")
        void shouldThrowExceptionWhenNoteNotFound() {
            var request = GoalNoteRequest.builder().content("Updated").build();

            when(goalRepository.findByIdAndOwnerAndArchivedAtIsNull(1L, user)).thenReturn(Optional.of(goal));
            when(goalNoteRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> goalNoteService.updateNote(1L, 1L, user, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Delete note tests")
    class DeleteNoteTests {

        @Test
        @DisplayName("Should delete note successfully")
        void shouldDeleteNoteSuccessfully() {
            when(goalRepository.findByIdAndOwnerAndArchivedAtIsNull(1L, user)).thenReturn(Optional.of(goal));
            when(goalNoteRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.of(note));

            goalNoteService.deleteNote(1L, 1L, user);

            verify(goalNoteRepository).delete(note);
        }
    }
}
