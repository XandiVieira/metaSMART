package com.relyon.metasmart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.entity.actionplan.*;
import com.relyon.metasmart.entity.actionplan.dto.ScheduledTaskDto;
import com.relyon.metasmart.entity.actionplan.dto.ScheduledTaskRequest;
import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.DuplicateResourceException;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.mapper.ScheduledTaskMapper;
import com.relyon.metasmart.repository.ActionItemRepository;
import com.relyon.metasmart.repository.GoalRepository;
import com.relyon.metasmart.repository.ScheduledTaskRepository;
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

@ExtendWith(MockitoExtension.class)
class ScheduledTaskServiceTest {

    @Mock
    private ScheduledTaskRepository scheduledTaskRepository;

    @Mock
    private ActionItemRepository actionItemRepository;

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private ScheduledTaskMapper scheduledTaskMapper;

    @InjectMocks
    private ScheduledTaskService scheduledTaskService;

    private User user;
    private Goal goal;
    private ActionItem actionItem;
    private ScheduledTask scheduledTask;
    private ScheduledTaskDto scheduledTaskDto;

    @BeforeEach
    void setUp() {
        scheduledTaskService.setSelf(scheduledTaskService);

        user = User.builder().id(1L).name("John").email("john@test.com").build();
        goal = Goal.builder().id(1L).title("Run 5K").owner(user).build();
        actionItem = ActionItem.builder()
                .id(1L)
                .title("Morning Run")
                .goal(goal)
                .taskType(TaskType.FREQUENCY_BASED)
                .build();
        scheduledTask = ScheduledTask.builder()
                .id(1L)
                .actionItem(actionItem)
                .scheduledDate(LocalDate.now())
                .completed(false)
                .build();
        scheduledTaskDto = ScheduledTaskDto.builder()
                .id(1L)
                .taskId(1L)
                .scheduledDate(LocalDate.now())
                .completed(false)
                .build();
    }

    @Nested
    @DisplayName("Create scheduled task tests")
    class CreateScheduledTaskTests {

        @Test
        @DisplayName("Should create scheduled task successfully")
        void shouldCreateScheduledTaskSuccessfully() {
            var request = ScheduledTaskRequest.builder()
                    .taskId(1L)
                    .scheduledDate(LocalDate.now())
                    .build();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(actionItemRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.of(actionItem));
            when(scheduledTaskRepository.findByActionItemAndScheduledDate(any(), any())).thenReturn(Optional.empty());
            when(scheduledTaskRepository.save(any())).thenReturn(scheduledTask);
            when(scheduledTaskMapper.toDto(scheduledTask)).thenReturn(scheduledTaskDto);

            var result = scheduledTaskService.createScheduledTask(1L, request, user);

            assertThat(result).isNotNull();
            assertThat(result.getTaskId()).isEqualTo(1L);
            verify(scheduledTaskRepository).save(any());
        }

        @Test
        @DisplayName("Should throw when goal not found")
        void shouldThrowWhenGoalNotFound() {
            var request = ScheduledTaskRequest.builder().taskId(1L).scheduledDate(LocalDate.now()).build();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> scheduledTaskService.createScheduledTask(1L, request, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.GOAL_NOT_FOUND);
        }

        @Test
        @DisplayName("Should throw when action item not found")
        void shouldThrowWhenActionItemNotFound() {
            var request = ScheduledTaskRequest.builder().taskId(1L).scheduledDate(LocalDate.now()).build();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(actionItemRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> scheduledTaskService.createScheduledTask(1L, request, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.ACTION_ITEM_NOT_FOUND);
        }

        @Test
        @DisplayName("Should throw when scheduled task already exists")
        void shouldThrowWhenScheduledTaskAlreadyExists() {
            var request = ScheduledTaskRequest.builder().taskId(1L).scheduledDate(LocalDate.now()).build();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(actionItemRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.of(actionItem));
            when(scheduledTaskRepository.findByActionItemAndScheduledDate(any(), any())).thenReturn(Optional.of(scheduledTask));

            assertThatThrownBy(() -> scheduledTaskService.createScheduledTask(1L, request, user))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessage(ErrorMessages.SCHEDULED_TASK_ALREADY_EXISTS);
        }
    }

    @Nested
    @DisplayName("Create bulk scheduled tasks tests")
    class CreateBulkScheduledTasksTests {

        @Test
        @DisplayName("Should create multiple scheduled tasks")
        void shouldCreateMultipleScheduledTasks() {
            var dates = List.of(LocalDate.now(), LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(actionItemRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.of(actionItem));
            when(scheduledTaskRepository.findByActionItemAndScheduledDate(any(), any())).thenReturn(Optional.empty());
            when(scheduledTaskRepository.save(any())).thenReturn(scheduledTask);
            when(scheduledTaskMapper.toDto(any())).thenReturn(scheduledTaskDto);

            var result = scheduledTaskService.createBulkScheduledTasks(1L, 1L, dates, user);

            assertThat(result).hasSize(3);
            verify(scheduledTaskRepository, times(3)).save(any());
        }

        @Test
        @DisplayName("Should skip existing scheduled tasks")
        void shouldSkipExistingScheduledTasks() {
            var dates = List.of(LocalDate.now(), LocalDate.now().plusDays(1));

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(actionItemRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.of(actionItem));
            when(scheduledTaskRepository.findByActionItemAndScheduledDate(actionItem, LocalDate.now()))
                    .thenReturn(Optional.of(scheduledTask));
            when(scheduledTaskRepository.findByActionItemAndScheduledDate(actionItem, LocalDate.now().plusDays(1)))
                    .thenReturn(Optional.empty());
            when(scheduledTaskRepository.save(any())).thenReturn(scheduledTask);
            when(scheduledTaskMapper.toDto(any())).thenReturn(scheduledTaskDto);

            var result = scheduledTaskService.createBulkScheduledTasks(1L, 1L, dates, user);

            assertThat(result).hasSize(1);
            verify(scheduledTaskRepository, times(1)).save(any());
        }
    }

    @Nested
    @DisplayName("Generate schedule for frequency task tests")
    class GenerateScheduleTests {

        @Test
        @DisplayName("Should generate schedule for frequency-based task with fixed days")
        void shouldGenerateScheduleWithFixedDays() {
            var frequencyGoal = FrequencyGoal.builder()
                    .count(3)
                    .period(FrequencyPeriod.WEEK)
                    .fixedDays("1,3,5")
                    .build();
            actionItem.setFrequencyGoal(frequencyGoal);

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(actionItemRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.of(actionItem));
            when(scheduledTaskRepository.findByActionItemAndScheduledDate(any(), any())).thenReturn(Optional.empty());
            when(scheduledTaskRepository.save(any())).thenReturn(scheduledTask);
            when(scheduledTaskMapper.toDto(any())).thenReturn(scheduledTaskDto);

            var result = scheduledTaskService.generateScheduleForFrequencyTask(
                    1L, 1L, LocalDate.now(), LocalDate.now().plusWeeks(1), user);

            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("Should generate schedule for frequency-based task without fixed days")
        void shouldGenerateScheduleWithoutFixedDays() {
            var frequencyGoal = FrequencyGoal.builder()
                    .count(3)
                    .period(FrequencyPeriod.WEEK)
                    .build();
            actionItem.setFrequencyGoal(frequencyGoal);

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(actionItemRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.of(actionItem));
            when(scheduledTaskRepository.findByActionItemAndScheduledDate(any(), any())).thenReturn(Optional.empty());
            when(scheduledTaskRepository.save(any())).thenReturn(scheduledTask);
            when(scheduledTaskMapper.toDto(any())).thenReturn(scheduledTaskDto);

            var result = scheduledTaskService.generateScheduleForFrequencyTask(
                    1L, 1L, LocalDate.now(), LocalDate.now().plusWeeks(1), user);

            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("Should return empty for non-frequency task")
        void shouldReturnEmptyForNonFrequencyTask() {
            actionItem.setTaskType(TaskType.ONE_TIME);
            actionItem.setFrequencyGoal(null);

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(actionItemRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.of(actionItem));

            var result = scheduledTaskService.generateScheduleForFrequencyTask(
                    1L, 1L, LocalDate.now(), LocalDate.now().plusWeeks(1), user);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should generate schedule with monthly period")
        void shouldGenerateScheduleWithMonthlyPeriod() {
            var frequencyGoal = FrequencyGoal.builder()
                    .count(4)
                    .period(FrequencyPeriod.MONTH)
                    .build();
            actionItem.setFrequencyGoal(frequencyGoal);

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(actionItemRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.of(actionItem));
            when(scheduledTaskRepository.findByActionItemAndScheduledDate(any(), any())).thenReturn(Optional.empty());
            when(scheduledTaskRepository.save(any())).thenReturn(scheduledTask);
            when(scheduledTaskMapper.toDto(any())).thenReturn(scheduledTaskDto);

            var result = scheduledTaskService.generateScheduleForFrequencyTask(
                    1L, 1L, LocalDate.now(), LocalDate.now().plusMonths(1), user);

            assertThat(result).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Get scheduled tasks tests")
    class GetScheduledTasksTests {

        @Test
        @DisplayName("Should get all scheduled tasks by goal")
        void shouldGetAllScheduledTasksByGoal() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(scheduledTaskRepository.findByGoalOrderByScheduledDateAsc(goal)).thenReturn(List.of(scheduledTask));
            when(scheduledTaskMapper.toDto(scheduledTask)).thenReturn(scheduledTaskDto);

            var result = scheduledTaskService.getScheduledTasksByGoal(1L, user);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should get scheduled tasks by date range")
        void shouldGetScheduledTasksByDateRange() {
            var startDate = LocalDate.now();
            var endDate = LocalDate.now().plusDays(7);

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(scheduledTaskRepository.findByGoalAndDateRange(goal, startDate, endDate))
                    .thenReturn(List.of(scheduledTask));
            when(scheduledTaskMapper.toDto(scheduledTask)).thenReturn(scheduledTaskDto);

            var result = scheduledTaskService.getScheduledTasksByDateRange(1L, startDate, endDate, user);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should get scheduled tasks by action item")
        void shouldGetScheduledTasksByActionItem() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(actionItemRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.of(actionItem));
            when(scheduledTaskRepository.findByActionItemOrderByScheduledDateAsc(actionItem))
                    .thenReturn(List.of(scheduledTask));
            when(scheduledTaskMapper.toDto(scheduledTask)).thenReturn(scheduledTaskDto);

            var result = scheduledTaskService.getScheduledTasksByActionItem(1L, 1L, user);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should get pending tasks")
        void shouldGetPendingTasks() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(scheduledTaskRepository.findPendingByGoalUntilDate(eq(goal), any()))
                    .thenReturn(List.of(scheduledTask));
            when(scheduledTaskMapper.toDto(scheduledTask)).thenReturn(scheduledTaskDto);

            var result = scheduledTaskService.getPendingTasks(1L, user);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Mark task completion tests")
    class MarkCompletionTests {

        @Test
        @DisplayName("Should mark task as completed")
        void shouldMarkTaskAsCompleted() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(scheduledTaskRepository.findById(1L)).thenReturn(Optional.of(scheduledTask));
            when(scheduledTaskRepository.save(any())).thenReturn(scheduledTask);
            when(scheduledTaskMapper.toDto(any())).thenReturn(scheduledTaskDto);

            var result = scheduledTaskService.markAsCompleted(1L, 1L, user);

            assertThat(result).isNotNull();
            verify(scheduledTaskRepository).save(argThat(task -> task.getCompleted()));
        }

        @Test
        @DisplayName("Should mark task as incomplete")
        void shouldMarkTaskAsIncomplete() {
            scheduledTask.setCompleted(true);
            scheduledTask.setCompletedAt(LocalDateTime.now());

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(scheduledTaskRepository.findById(1L)).thenReturn(Optional.of(scheduledTask));
            when(scheduledTaskRepository.save(any())).thenReturn(scheduledTask);
            when(scheduledTaskMapper.toDto(any())).thenReturn(scheduledTaskDto);

            var result = scheduledTaskService.markAsIncomplete(1L, 1L, user);

            assertThat(result).isNotNull();
            verify(scheduledTaskRepository).save(argThat(task -> !task.getCompleted() && task.getCompletedAt() == null));
        }

        @Test
        @DisplayName("Should throw when scheduled task not found")
        void shouldThrowWhenScheduledTaskNotFound() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(scheduledTaskRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> scheduledTaskService.markAsCompleted(1L, 1L, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.SCHEDULED_TASK_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Delete scheduled task tests")
    class DeleteScheduledTaskTests {

        @Test
        @DisplayName("Should delete scheduled task")
        void shouldDeleteScheduledTask() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(scheduledTaskRepository.findById(1L)).thenReturn(Optional.of(scheduledTask));

            scheduledTaskService.deleteScheduledTask(1L, 1L, user);

            verify(scheduledTaskRepository).delete(scheduledTask);
        }

        @Test
        @DisplayName("Should throw when scheduled task not found for delete")
        void shouldThrowWhenScheduledTaskNotFoundForDelete() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(scheduledTaskRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> scheduledTaskService.deleteScheduledTask(1L, 1L, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.SCHEDULED_TASK_NOT_FOUND);
        }
    }
}
