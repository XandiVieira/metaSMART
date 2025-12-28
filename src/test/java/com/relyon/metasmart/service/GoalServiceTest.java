package com.relyon.metasmart.service;

import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.goal.GoalCategory;
import com.relyon.metasmart.entity.goal.GoalStatus;
import com.relyon.metasmart.entity.goal.dto.GoalRequest;
import com.relyon.metasmart.entity.goal.dto.GoalResponse;
import com.relyon.metasmart.entity.goal.dto.SmartPillarsDto;
import com.relyon.metasmart.entity.goal.dto.UpdateGoalRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.mapper.GoalMapper;
import com.relyon.metasmart.repository.ActionItemRepository;
import com.relyon.metasmart.repository.GoalRepository;
import com.relyon.metasmart.repository.MilestoneRepository;
import com.relyon.metasmart.repository.ObstacleEntryRepository;
import com.relyon.metasmart.repository.ProgressEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private MilestoneRepository milestoneRepository;

    @Mock
    private ProgressEntryRepository progressEntryRepository;

    @Mock
    private ActionItemRepository actionItemRepository;

    @Mock
    private ObstacleEntryRepository obstacleEntryRepository;

    @Mock
    private GoalMapper goalMapper;

    @InjectMocks
    private GoalService goalService;

    private User user;
    private Goal goal;
    private GoalRequest goalRequest;
    private GoalResponse goalResponse;

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
                .motivation("Improve health")
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusMonths(3))
                .goalStatus(GoalStatus.ACTIVE)
                .owner(user)
                .build();

        goalRequest = GoalRequest.builder()
                .title("Run 5km")
                .description("Build endurance to run 5km")
                .goalCategory(GoalCategory.HEALTH)
                .targetValue("5")
                .unit("km")
                .motivation("Improve health")
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusMonths(3))
                .build();

        goalResponse = GoalResponse.builder()
                .id(1L)
                .title("Run 5km")
                .description("Build endurance to run 5km")
                .goalCategory(GoalCategory.HEALTH)
                .targetValue("5")
                .unit("km")
                .currentProgress(BigDecimal.ZERO)
                .motivation("Improve health")
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusMonths(3))
                .goalStatus(GoalStatus.ACTIVE)
                .build();
    }

    @Nested
    @DisplayName("Create goal tests")
    class CreateGoalTests {

        @Test
        @DisplayName("Should create goal successfully")
        void shouldCreateGoalSuccessfully() {
            when(goalMapper.toEntity(goalRequest)).thenReturn(goal);
            when(goalRepository.save(any(Goal.class))).thenReturn(goal);
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(Collections.emptyList());

            var response = goalService.create(goalRequest, user);

            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo(goal.getTitle());
            verify(goalRepository).save(any(Goal.class));
            verify(milestoneRepository, times(4)).save(any());
        }
    }

    @Nested
    @DisplayName("Find goal tests")
    class FindGoalTests {

        @Test
        @DisplayName("Should find goal by id")
        void shouldFindGoalById() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(Collections.emptyList());

            var response = goalService.findById(1L, user);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            verify(goalRepository).findByIdAndOwner(1L, user);
        }

        @Test
        @DisplayName("Should throw exception when goal not found")
        void shouldThrowExceptionWhenGoalNotFound() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> goalService.findById(1L, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.GOAL_NOT_FOUND);
        }

        @Test
        @DisplayName("Should find all goals")
        void shouldFindAllGoals() {
            var pageable = Pageable.unpaged();
            var goals = new PageImpl<>(List.of(goal));
            when(goalRepository.findByOwner(user, pageable)).thenReturn(goals);
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(Collections.emptyList());

            var response = goalService.findAll(user, pageable);

            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should find goals by status")
        void shouldFindGoalsByStatus() {
            var pageable = Pageable.unpaged();
            var goals = new PageImpl<>(List.of(goal));
            when(goalRepository.findByOwnerAndGoalStatus(user, GoalStatus.ACTIVE, pageable)).thenReturn(goals);
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(Collections.emptyList());

            var response = goalService.findByStatus(user, GoalStatus.ACTIVE, pageable);

            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should find goals by category")
        void shouldFindGoalsByCategory() {
            var pageable = Pageable.unpaged();
            var goals = new PageImpl<>(List.of(goal));
            when(goalRepository.findByOwnerAndGoalCategory(user, GoalCategory.HEALTH, pageable)).thenReturn(goals);
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(Collections.emptyList());

            var response = goalService.findByCategory(user, GoalCategory.HEALTH, pageable);

            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Update goal tests")
    class UpdateGoalTests {

        @Test
        @DisplayName("Should update goal successfully")
        void shouldUpdateGoalSuccessfully() {
            var updateRequest = UpdateGoalRequest.builder()
                    .title("Updated title")
                    .build();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(goalRepository.save(any(Goal.class))).thenReturn(goal);
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(Collections.emptyList());

            var response = goalService.update(1L, updateRequest, user);

            assertThat(response).isNotNull();
            verify(goalRepository).save(any(Goal.class));
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent goal")
        void shouldThrowExceptionWhenUpdatingNonExistentGoal() {
            var updateRequest = UpdateGoalRequest.builder().title("Updated").build();
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> goalService.update(1L, updateRequest, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.GOAL_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Delete goal tests")
    class DeleteGoalTests {

        @Test
        @DisplayName("Should delete goal successfully")
        void shouldDeleteGoalSuccessfully() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));

            goalService.delete(1L, user);

            verify(obstacleEntryRepository).deleteByGoal(goal);
            verify(actionItemRepository).deleteByGoal(goal);
            verify(progressEntryRepository).deleteByGoal(goal);
            verify(milestoneRepository).deleteByGoal(goal);
            verify(goalRepository).delete(goal);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent goal")
        void shouldThrowExceptionWhenDeletingNonExistentGoal() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> goalService.delete(1L, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.GOAL_NOT_FOUND);
        }
    }
}
