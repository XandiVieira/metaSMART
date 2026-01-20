package com.relyon.metasmart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.entity.actionplan.ActionItem;
import com.relyon.metasmart.entity.actionplan.CompletionStatus;
import com.relyon.metasmart.entity.actionplan.TaskCompletion;
import com.relyon.metasmart.entity.actionplan.dto.TaskCompletionDto;
import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.mapper.TaskCompletionMapper;
import com.relyon.metasmart.repository.ActionItemRepository;
import com.relyon.metasmart.repository.GoalRepository;
import com.relyon.metasmart.repository.TaskCompletionRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
class TaskCompletionServiceTest {

    @Mock
    private TaskCompletionRepository taskCompletionRepository;

    @Mock
    private ActionItemRepository actionItemRepository;

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private TaskCompletionMapper taskCompletionMapper;

    @Mock
    private StreakService streakService;

    @InjectMocks
    private TaskCompletionService taskCompletionService;

    private User user;
    private Goal goal;
    private ActionItem actionItem;
    private TaskCompletion taskCompletion;
    private TaskCompletionDto taskCompletionDto;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("John").email("john@test.com").build();
        goal = Goal.builder().id(1L).title("Run 5K").owner(user).build();
        actionItem = ActionItem.builder().id(1L).title("Morning Run").goal(goal).build();
        taskCompletion = TaskCompletion.builder()
                .id(1L)
                .actionItem(actionItem)
                .scheduledDate(LocalDate.now())
                .periodStart(LocalDate.now().withDayOfMonth(1))
                .status(CompletionStatus.COMPLETED)
                .completedAt(LocalDateTime.now())
                .note("Great workout!")
                .build();
        taskCompletionDto = TaskCompletionDto.builder()
                .id(1L)
                .scheduledDate(LocalDate.now())
                .status(CompletionStatus.COMPLETED)
                .completedAt(LocalDateTime.now())
                .note("Great workout!")
                .build();
    }

    @Nested
    @DisplayName("Record completion tests")
    class RecordCompletionTests {

        @Test
        @DisplayName("Should record completion for today")
        void shouldRecordCompletionForToday() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(actionItemRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.of(actionItem));
            when(taskCompletionRepository.save(any())).thenReturn(taskCompletion);
            when(taskCompletionMapper.toDto(taskCompletion)).thenReturn(taskCompletionDto);

            var result = taskCompletionService.recordCompletion(1L, 1L, "Great workout!", user);

            assertThat(result).isNotNull();
            assertThat(result.getNote()).isEqualTo("Great workout!");
            verify(taskCompletionRepository).save(any());
        }

        @Test
        @DisplayName("Should record completion for specific date")
        void shouldRecordCompletionForSpecificDate() {
            var specificDate = LocalDate.now().minusDays(1);

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(actionItemRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.of(actionItem));
            when(taskCompletionRepository.save(any())).thenReturn(taskCompletion);
            when(taskCompletionMapper.toDto(taskCompletion)).thenReturn(taskCompletionDto);

            var result = taskCompletionService.recordCompletionForDate(1L, 1L, specificDate, "Missed yesterday", user);

            assertThat(result).isNotNull();
            verify(taskCompletionRepository).save(argThat(tc -> tc.getScheduledDate().equals(specificDate)));
        }

        @Test
        @DisplayName("Should throw when goal not found")
        void shouldThrowWhenGoalNotFound() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskCompletionService.recordCompletion(1L, 1L, "note", user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.GOAL_NOT_FOUND);
        }

        @Test
        @DisplayName("Should throw when action item not found")
        void shouldThrowWhenActionItemNotFound() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(actionItemRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskCompletionService.recordCompletion(1L, 1L, "note", user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.ACTION_ITEM_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Get completion history tests")
    class GetCompletionHistoryTests {

        @Test
        @DisplayName("Should get completion history")
        void shouldGetCompletionHistory() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(actionItemRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.of(actionItem));
            when(taskCompletionRepository.findByActionItemOrderByCompletedAtDesc(actionItem))
                    .thenReturn(List.of(taskCompletion));
            when(taskCompletionMapper.toDto(taskCompletion)).thenReturn(taskCompletionDto);

            var result = taskCompletionService.getCompletionHistory(1L, 1L, user);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should get paginated completion history")
        void shouldGetPaginatedCompletionHistory() {
            var pageable = Pageable.ofSize(10);
            var page = new PageImpl<>(List.of(taskCompletion));

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(actionItemRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.of(actionItem));
            when(taskCompletionRepository.findByActionItemOrderByCompletedAtDesc(actionItem, pageable))
                    .thenReturn(page);
            when(taskCompletionMapper.toDto(taskCompletion)).thenReturn(taskCompletionDto);

            var result = taskCompletionService.getCompletionHistoryPaginated(1L, 1L, user, pageable);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should get completions by date range")
        void shouldGetCompletionsByDateRange() {
            var startDate = LocalDate.now().minusDays(7);
            var endDate = LocalDate.now();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(actionItemRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.of(actionItem));
            when(taskCompletionRepository.findByActionItemAndScheduledDateBetween(actionItem, startDate, endDate))
                    .thenReturn(List.of(taskCompletion));
            when(taskCompletionMapper.toDto(taskCompletion)).thenReturn(taskCompletionDto);

            var result = taskCompletionService.getCompletionsByDateRange(1L, 1L, startDate, endDate, user);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Count completions tests")
    class CountCompletionsTests {

        @Test
        @DisplayName("Should count all completions")
        void shouldCountAllCompletions() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(actionItemRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.of(actionItem));
            when(taskCompletionRepository.countByActionItem(actionItem)).thenReturn(10L);

            var result = taskCompletionService.countCompletions(1L, 1L, user);

            assertThat(result).isEqualTo(10);
        }

        @Test
        @DisplayName("Should count completions in period")
        void shouldCountCompletionsInPeriod() {
            var startDate = LocalDate.now().minusDays(7);
            var endDate = LocalDate.now();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(actionItemRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.of(actionItem));
            when(taskCompletionRepository.countByActionItemAndScheduledDateBetween(actionItem, startDate, endDate))
                    .thenReturn(5L);

            var result = taskCompletionService.countCompletionsInPeriod(1L, 1L, startDate, endDate, user);

            assertThat(result).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Delete completion tests")
    class DeleteCompletionTests {

        @Test
        @DisplayName("Should delete completion")
        void shouldDeleteCompletion() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(actionItemRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.of(actionItem));
            when(taskCompletionRepository.findById(1L)).thenReturn(Optional.of(taskCompletion));

            taskCompletionService.deleteCompletion(1L, 1L, 1L, user);

            verify(taskCompletionRepository).delete(taskCompletion);
        }

        @Test
        @DisplayName("Should throw when completion not found")
        void shouldThrowWhenCompletionNotFound() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(actionItemRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.of(actionItem));
            when(taskCompletionRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskCompletionService.deleteCompletion(1L, 1L, 1L, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.TASK_COMPLETION_NOT_FOUND);
        }
    }
}
