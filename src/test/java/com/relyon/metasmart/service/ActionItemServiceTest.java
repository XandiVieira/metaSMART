package com.relyon.metasmart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.entity.actionplan.ActionItem;
import com.relyon.metasmart.entity.actionplan.dto.ActionItemRequest;
import com.relyon.metasmart.entity.actionplan.dto.ActionItemResponse;
import com.relyon.metasmart.entity.actionplan.dto.UpdateActionItemRequest;
import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.mapper.ActionItemMapper;
import com.relyon.metasmart.mapper.TaskCompletionMapper;
import com.relyon.metasmart.repository.ActionItemRepository;
import com.relyon.metasmart.repository.GoalRepository;
import com.relyon.metasmart.repository.ScheduledTaskRepository;
import com.relyon.metasmart.repository.StreakInfoRepository;
import com.relyon.metasmart.repository.TaskCompletionRepository;
import com.relyon.metasmart.repository.TaskScheduleSlotRepository;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActionItemServiceTest {

    @Mock
    private ActionItemRepository actionItemRepository;

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private TaskCompletionRepository taskCompletionRepository;

    @Mock
    private ScheduledTaskRepository scheduledTaskRepository;

    @Mock
    private TaskScheduleSlotRepository taskScheduleSlotRepository;

    @Mock
    private StreakInfoRepository streakInfoRepository;

    @Mock
    private ActionItemMapper actionItemMapper;

    @Mock
    private TaskCompletionMapper taskCompletionMapper;

    @InjectMocks
    private ActionItemService actionItemService;

    private User user;
    private Goal goal;
    private ActionItem actionItem;
    private ActionItemRequest request;
    private ActionItemResponse response;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("John").email("john@test.com").build();
        goal = Goal.builder().id(1L).title("Run 5km").owner(user).build();

        actionItem = ActionItem.builder()
                .id(1L)
                .goal(goal)
                .title("Buy running shoes")
                .description("Get proper running shoes")
                .targetDate(LocalDate.now().plusDays(7))
                .orderIndex(1)
                .completed(false)
                .build();

        request = ActionItemRequest.builder()
                .title("Buy running shoes")
                .description("Get proper running shoes")
                .targetDate(LocalDate.now().plusDays(7))
                .orderIndex(1)
                .build();

        response = ActionItemResponse.builder()
                .id(1L)
                .title("Buy running shoes")
                .description("Get proper running shoes")
                .targetDate(LocalDate.now().plusDays(7))
                .orderIndex(1)
                .completed(false)
                .build();
    }

    @Nested
    @DisplayName("Create action item tests")
    class CreateTests {

        @Test
        @DisplayName("Should create action item successfully")
        void shouldCreateActionItemSuccessfully() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(actionItemMapper.toEntity(request)).thenReturn(actionItem);
            when(actionItemRepository.save(any(ActionItem.class))).thenReturn(actionItem);
            when(actionItemMapper.toResponse(actionItem)).thenReturn(response);
            when(taskCompletionRepository.findByActionItemOrderByCompletedAtDesc(any())).thenReturn(Collections.emptyList());

            var result = actionItemService.create(1L, request, user);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Buy running shoes");
            verify(actionItemRepository).save(any(ActionItem.class));
        }

        @Test
        @DisplayName("Should throw exception when goal not found")
        void shouldThrowExceptionWhenGoalNotFound() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> actionItemService.create(1L, request, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.GOAL_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Find action items tests")
    class FindTests {

        @Test
        @DisplayName("Should find action items by goal")
        void shouldFindActionItemsByGoal() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(actionItemRepository.findByGoalOrderByOrderIndexAscCreatedAtAsc(goal))
                    .thenReturn(List.of(actionItem));
            when(actionItemMapper.toResponse(actionItem)).thenReturn(response);
            when(taskCompletionRepository.findByActionItemOrderByCompletedAtDesc(any())).thenReturn(Collections.emptyList());

            var result = actionItemService.findByGoal(1L, user);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Buy running shoes");
        }

        @Test
        @DisplayName("Should throw exception when goal not found for findByGoal")
        void shouldThrowExceptionWhenGoalNotFoundForFindByGoal() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> actionItemService.findByGoal(1L, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.GOAL_NOT_FOUND);
        }

        @Test
        @DisplayName("Should find action item by id")
        void shouldFindActionItemById() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(actionItemRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.of(actionItem));
            when(actionItemMapper.toResponse(actionItem)).thenReturn(response);
            when(taskCompletionRepository.findByActionItemOrderByCompletedAtDesc(any())).thenReturn(Collections.emptyList());

            var result = actionItemService.findById(1L, 1L, user);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Buy running shoes");
        }

        @Test
        @DisplayName("Should throw exception when goal not found for findById")
        void shouldThrowExceptionWhenGoalNotFoundForFindById() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> actionItemService.findById(1L, 1L, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.GOAL_NOT_FOUND);
        }

        @Test
        @DisplayName("Should throw exception when action item not found for findById")
        void shouldThrowExceptionWhenActionItemNotFoundForFindById() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(actionItemRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> actionItemService.findById(1L, 1L, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.ACTION_ITEM_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Update action item tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update action item successfully")
        void shouldUpdateActionItemSuccessfully() {
            var updateRequest = UpdateActionItemRequest.builder()
                    .title("Updated title")
                    .completed(true)
                    .build();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(actionItemRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.of(actionItem));
            when(actionItemRepository.save(any(ActionItem.class))).thenReturn(actionItem);
            when(actionItemMapper.toResponse(actionItem)).thenReturn(response);
            when(taskCompletionRepository.findByActionItemOrderByCompletedAtDesc(any())).thenReturn(Collections.emptyList());

            var result = actionItemService.update(1L, 1L, updateRequest, user);

            assertThat(result).isNotNull();
            verify(actionItemRepository).save(any(ActionItem.class));
        }

        @Test
        @DisplayName("Should throw exception when action item not found")
        void shouldThrowExceptionWhenActionItemNotFound() {
            var updateRequest = UpdateActionItemRequest.builder().build();
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(actionItemRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> actionItemService.update(1L, 1L, updateRequest, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.ACTION_ITEM_NOT_FOUND);
        }

        @Test
        @DisplayName("Should throw exception when goal not found for update")
        void shouldThrowExceptionWhenGoalNotFoundForUpdate() {
            var updateRequest = UpdateActionItemRequest.builder().build();
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> actionItemService.update(1L, 1L, updateRequest, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.GOAL_NOT_FOUND);
        }

        @Test
        @DisplayName("Should update action item with context and dependencies")
        void shouldUpdateActionItemWithContextAndDependencies() {
            var updateRequest = UpdateActionItemRequest.builder()
                    .context(List.of("home", "weekend"))
                    .dependencies(List.of(2L, 3L))
                    .build();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(actionItemRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.of(actionItem));
            when(actionItemRepository.save(any(ActionItem.class))).thenReturn(actionItem);
            when(actionItemMapper.toResponse(actionItem)).thenReturn(response);
            when(taskCompletionRepository.findByActionItemOrderByCompletedAtDesc(any())).thenReturn(Collections.emptyList());

            actionItemService.update(1L, 1L, updateRequest, user);

            assertThat(actionItem.getContext()).isEqualTo("home,weekend");
            assertThat(actionItem.getDependencies()).isEqualTo("2,3");
            verify(actionItemRepository).save(actionItem);
        }

        @Test
        @DisplayName("Should clear completedAt when marking action item as incomplete")
        void shouldClearCompletedAtWhenMarkingAsIncomplete() {
            actionItem.setCompleted(true);
            actionItem.setCompletedAt(java.time.LocalDateTime.now());

            var updateRequest = UpdateActionItemRequest.builder()
                    .completed(false)
                    .build();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(actionItemRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.of(actionItem));
            when(actionItemRepository.save(any(ActionItem.class))).thenReturn(actionItem);
            when(actionItemMapper.toResponse(actionItem)).thenReturn(response);
            when(taskCompletionRepository.findByActionItemOrderByCompletedAtDesc(any())).thenReturn(Collections.emptyList());

            actionItemService.update(1L, 1L, updateRequest, user);

            assertThat(actionItem.getCompleted()).isFalse();
            assertThat(actionItem.getCompletedAt()).isNull();
        }

        @Test
        @DisplayName("Should set completedAt when marking action item as complete")
        void shouldSetCompletedAtWhenMarkingAsComplete() {
            actionItem.setCompleted(false);
            actionItem.setCompletedAt(null);

            var updateRequest = UpdateActionItemRequest.builder()
                    .completed(true)
                    .build();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(actionItemRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.of(actionItem));
            when(actionItemRepository.save(any(ActionItem.class))).thenReturn(actionItem);
            when(actionItemMapper.toResponse(actionItem)).thenReturn(response);
            when(taskCompletionRepository.findByActionItemOrderByCompletedAtDesc(any())).thenReturn(Collections.emptyList());

            actionItemService.update(1L, 1L, updateRequest, user);

            assertThat(actionItem.getCompleted()).isTrue();
            assertThat(actionItem.getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should handle empty dependencies list")
        void shouldHandleEmptyDependenciesList() {
            var updateRequest = UpdateActionItemRequest.builder()
                    .dependencies(Collections.emptyList())
                    .build();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(actionItemRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.of(actionItem));
            when(actionItemRepository.save(any(ActionItem.class))).thenReturn(actionItem);
            when(actionItemMapper.toResponse(actionItem)).thenReturn(response);
            when(taskCompletionRepository.findByActionItemOrderByCompletedAtDesc(any())).thenReturn(Collections.emptyList());

            actionItemService.update(1L, 1L, updateRequest, user);

            assertThat(actionItem.getDependencies()).isNull();
        }
    }

    @Nested
    @DisplayName("Delete action item tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete action item successfully")
        void shouldDeleteActionItemSuccessfully() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(actionItemRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.of(actionItem));
            doNothing().when(streakInfoRepository).deleteByActionItem(any());
            doNothing().when(scheduledTaskRepository).deleteByActionItem(any());
            doNothing().when(taskScheduleSlotRepository).deleteByActionItem(any());
            doNothing().when(taskCompletionRepository).deleteByActionItem(any());

            actionItemService.delete(1L, 1L, user);

            verify(streakInfoRepository).deleteByActionItem(actionItem);
            verify(scheduledTaskRepository).deleteByActionItem(actionItem);
            verify(taskCompletionRepository).deleteByActionItem(actionItem);
            verify(taskScheduleSlotRepository).deleteByActionItem(actionItem);
            verify(actionItemRepository).delete(actionItem);
        }

        @Test
        @DisplayName("Should delete task completions BEFORE schedule slots to avoid FK constraint violation on schedule_slot_id")
        void shouldDeleteCompletionsBeforeSlotsToAvoidFkViolation() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(actionItemRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.of(actionItem));
            doNothing().when(streakInfoRepository).deleteByActionItem(any());
            doNothing().when(scheduledTaskRepository).deleteByActionItem(any());
            doNothing().when(taskScheduleSlotRepository).deleteByActionItem(any());
            doNothing().when(taskCompletionRepository).deleteByActionItem(any());

            actionItemService.delete(1L, 1L, user);

            // Critical: task_completions.schedule_slot_id references task_schedule_slots.id
            // Completions MUST be deleted before slots, otherwise FK constraint fails
            var inOrderVerifier = inOrder(
                    streakInfoRepository,
                    scheduledTaskRepository,
                    taskCompletionRepository,
                    taskScheduleSlotRepository,
                    actionItemRepository
            );
            inOrderVerifier.verify(streakInfoRepository).deleteByActionItem(actionItem);
            inOrderVerifier.verify(scheduledTaskRepository).deleteByActionItem(actionItem);
            inOrderVerifier.verify(taskCompletionRepository).deleteByActionItem(actionItem);
            inOrderVerifier.verify(taskScheduleSlotRepository).deleteByActionItem(actionItem);
            inOrderVerifier.verify(actionItemRepository).delete(actionItem);
        }

        @Test
        @DisplayName("Should throw exception when action item not found")
        void shouldThrowExceptionWhenDeletingNonExistentItem() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(actionItemRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> actionItemService.delete(1L, 1L, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.ACTION_ITEM_NOT_FOUND);
        }

        @Test
        @DisplayName("Should throw exception when goal not found for delete")
        void shouldThrowExceptionWhenGoalNotFoundForDelete() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> actionItemService.delete(1L, 1L, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.GOAL_NOT_FOUND);
        }
    }
}
