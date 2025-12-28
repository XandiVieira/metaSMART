package com.relyon.metasmart.service;

import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.goal.GoalCategory;
import com.relyon.metasmart.entity.goal.GoalStatus;
import com.relyon.metasmart.entity.progress.Milestone;
import com.relyon.metasmart.entity.progress.ProgressEntry;
import com.relyon.metasmart.entity.progress.dto.*;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.DuplicateResourceException;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.mapper.MilestoneMapper;
import com.relyon.metasmart.mapper.ProgressEntryMapper;
import com.relyon.metasmart.repository.GoalRepository;
import com.relyon.metasmart.repository.MilestoneRepository;
import com.relyon.metasmart.repository.ProgressEntryRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgressServiceTest {

    @Mock
    private ProgressEntryRepository progressEntryRepository;

    @Mock
    private MilestoneRepository milestoneRepository;

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private ProgressEntryMapper progressEntryMapper;

    @Mock
    private MilestoneMapper milestoneMapper;

    @InjectMocks
    private ProgressService progressService;

    private User user;
    private Goal goal;
    private ProgressEntry progressEntry;
    private ProgressEntryRequest progressRequest;
    private ProgressEntryResponse progressResponse;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("John").email("john@test.com").build();

        goal = Goal.builder()
                .id(1L)
                .title("Run 5km")
                .targetValue("5")
                .unit("km")
                .currentProgress(BigDecimal.ZERO)
                .goalStatus(GoalStatus.ACTIVE)
                .owner(user)
                .build();

        progressEntry = ProgressEntry.builder()
                .id(1L)
                .goal(goal)
                .progressValue(BigDecimal.ONE)
                .note("Good run")
                .build();

        progressRequest = ProgressEntryRequest.builder()
                .progressValue(BigDecimal.ONE)
                .note("Good run")
                .build();

        progressResponse = ProgressEntryResponse.builder()
                .id(1L)
                .progressValue(BigDecimal.ONE)
                .note("Good run")
                .build();
    }

    @Nested
    @DisplayName("Add progress tests")
    class AddProgressTests {

        @Test
        @DisplayName("Should add progress successfully")
        void shouldAddProgressSuccessfully() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(progressEntryMapper.toEntity(progressRequest)).thenReturn(progressEntry);
            when(progressEntryRepository.save(any(ProgressEntry.class))).thenReturn(progressEntry);
            when(progressEntryMapper.toResponse(progressEntry)).thenReturn(progressResponse);
            when(progressEntryRepository.sumValueByGoal(goal)).thenReturn(BigDecimal.ONE);
            when(milestoneRepository.findByGoalAndAchievedFalseOrderByPercentageAsc(goal)).thenReturn(Collections.emptyList());

            var response = progressService.addProgress(1L, progressRequest, user);

            assertThat(response).isNotNull();
            assertThat(response.getProgressValue()).isEqualTo(BigDecimal.ONE);
            verify(progressEntryRepository).save(any(ProgressEntry.class));
            verify(goalRepository).save(any(Goal.class));
        }

        @Test
        @DisplayName("Should throw exception when goal not found")
        void shouldThrowExceptionWhenGoalNotFound() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> progressService.addProgress(1L, progressRequest, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.GOAL_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Get progress history tests")
    class GetProgressHistoryTests {

        @Test
        @DisplayName("Should get progress history")
        void shouldGetProgressHistory() {
            var pageable = Pageable.unpaged();
            var entries = new PageImpl<>(List.of(progressEntry));
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(progressEntryRepository.findByGoalOrderByCreatedAtDesc(goal, pageable)).thenReturn(entries);
            when(progressEntryMapper.toResponse(progressEntry)).thenReturn(progressResponse);

            var response = progressService.getProgressHistory(1L, user, pageable);

            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should get progress history by date range")
        void shouldGetProgressHistoryByDateRange() {
            var pageable = Pageable.unpaged();
            var startDate = LocalDate.now().minusDays(7);
            var endDate = LocalDate.now();
            var entries = new PageImpl<>(List.of(progressEntry));

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(progressEntryRepository.findByGoalAndCreatedAtBetweenOrderByCreatedAtDesc(
                    eq(goal), any(LocalDateTime.class), any(LocalDateTime.class), eq(pageable)
            )).thenReturn(entries);
            when(progressEntryMapper.toResponse(progressEntry)).thenReturn(progressResponse);

            var response = progressService.getProgressHistoryByDateRange(1L, user, startDate, endDate, pageable);

            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Update progress tests")
    class UpdateProgressTests {

        @Test
        @DisplayName("Should update progress entry")
        void shouldUpdateProgressEntry() {
            var updateRequest = UpdateProgressEntryRequest.builder()
                    .progressValue(BigDecimal.valueOf(2))
                    .note("Updated note")
                    .build();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(progressEntryRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.of(progressEntry));
            when(progressEntryRepository.save(any(ProgressEntry.class))).thenReturn(progressEntry);
            when(progressEntryMapper.toResponse(progressEntry)).thenReturn(progressResponse);
            when(progressEntryRepository.sumValueByGoal(goal)).thenReturn(BigDecimal.valueOf(2));
            when(milestoneRepository.findByGoalOrderByPercentageAsc(goal)).thenReturn(Collections.emptyList());

            var response = progressService.updateProgressEntry(1L, 1L, updateRequest, user);

            assertThat(response).isNotNull();
            verify(progressEntryRepository).save(any(ProgressEntry.class));
        }

        @Test
        @DisplayName("Should throw exception when entry not found")
        void shouldThrowExceptionWhenEntryNotFound() {
            var updateRequest = UpdateProgressEntryRequest.builder().build();
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(progressEntryRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> progressService.updateProgressEntry(1L, 1L, updateRequest, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.PROGRESS_ENTRY_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Delete progress tests")
    class DeleteProgressTests {

        @Test
        @DisplayName("Should delete progress entry")
        void shouldDeleteProgressEntry() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(progressEntryRepository.findById(1L)).thenReturn(Optional.of(progressEntry));
            when(progressEntryRepository.sumValueByGoal(goal)).thenReturn(BigDecimal.ZERO);
            when(milestoneRepository.findByGoalOrderByPercentageAsc(goal)).thenReturn(Collections.emptyList());

            progressService.deleteProgressEntry(1L, 1L, user);

            verify(progressEntryRepository).delete(progressEntry);
        }
    }

    @Nested
    @DisplayName("Milestone tests")
    class MilestoneTests {

        @Test
        @DisplayName("Should add milestone")
        void shouldAddMilestone() {
            var milestoneRequest = MilestoneRequest.builder()
                    .percentage(50)
                    .description("Halfway there!")
                    .build();

            var milestone = Milestone.builder()
                    .id(1L)
                    .goal(goal)
                    .percentage(50)
                    .description("Halfway there!")
                    .achieved(false)
                    .build();

            var milestoneResponse = MilestoneResponse.builder()
                    .id(1L)
                    .percentage(50)
                    .description("Halfway there!")
                    .achieved(false)
                    .build();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(milestoneRepository.existsByGoalAndPercentage(goal, 50)).thenReturn(false);
            when(milestoneMapper.toEntity(milestoneRequest)).thenReturn(milestone);
            when(milestoneRepository.save(any(Milestone.class))).thenReturn(milestone);
            when(milestoneMapper.toResponse(milestone)).thenReturn(milestoneResponse);

            var response = progressService.addMilestone(1L, milestoneRequest, user);

            assertThat(response).isNotNull();
            assertThat(response.getPercentage()).isEqualTo(50);
        }

        @Test
        @DisplayName("Should throw exception when milestone already exists")
        void shouldThrowExceptionWhenMilestoneExists() {
            var milestoneRequest = MilestoneRequest.builder()
                    .percentage(50)
                    .description("Halfway there!")
                    .build();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(milestoneRepository.existsByGoalAndPercentage(goal, 50)).thenReturn(true);

            assertThatThrownBy(() -> progressService.addMilestone(1L, milestoneRequest, user))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessage(ErrorMessages.MILESTONE_ALREADY_EXISTS);
        }

        @Test
        @DisplayName("Should get milestones")
        void shouldGetMilestones() {
            var milestone = Milestone.builder()
                    .id(1L)
                    .goal(goal)
                    .percentage(50)
                    .achieved(false)
                    .build();

            var milestoneResponse = MilestoneResponse.builder()
                    .id(1L)
                    .percentage(50)
                    .achieved(false)
                    .build();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(milestoneRepository.findByGoalOrderByPercentageAsc(goal)).thenReturn(List.of(milestone));
            when(milestoneMapper.toResponse(milestone)).thenReturn(milestoneResponse);

            var response = progressService.getMilestones(1L, user);

            assertThat(response).hasSize(1);
            assertThat(response.get(0).getPercentage()).isEqualTo(50);
        }

        @Test
        @DisplayName("Should delete milestone")
        void shouldDeleteMilestone() {
            var milestone = Milestone.builder()
                    .id(1L)
                    .goal(goal)
                    .percentage(50)
                    .build();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(milestoneRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.of(milestone));

            progressService.deleteMilestone(1L, 1L, user);

            verify(milestoneRepository).delete(milestone);
        }

        @Test
        @DisplayName("Should throw exception when milestone not found")
        void shouldThrowExceptionWhenMilestoneNotFound() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(milestoneRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> progressService.deleteMilestone(1L, 1L, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.MILESTONE_NOT_FOUND);
        }

        @Test
        @DisplayName("Should mark milestone as achieved immediately when progress is high enough")
        void shouldMarkMilestoneAsAchievedImmediately() {
            goal.setCurrentProgress(BigDecimal.valueOf(3));

            var milestoneRequest = MilestoneRequest.builder()
                    .percentage(50)
                    .description("Halfway there!")
                    .build();

            var milestone = Milestone.builder()
                    .id(1L)
                    .goal(goal)
                    .percentage(50)
                    .description("Halfway there!")
                    .achieved(false)
                    .build();

            var milestoneResponse = MilestoneResponse.builder()
                    .id(1L)
                    .percentage(50)
                    .description("Halfway there!")
                    .achieved(true)
                    .achievedAt(LocalDateTime.now())
                    .build();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(milestoneRepository.existsByGoalAndPercentage(goal, 50)).thenReturn(false);
            when(milestoneMapper.toEntity(milestoneRequest)).thenReturn(milestone);
            when(milestoneRepository.save(any(Milestone.class))).thenReturn(milestone);
            when(milestoneMapper.toResponse(any(Milestone.class))).thenReturn(milestoneResponse);

            var response = progressService.addMilestone(1L, milestoneRequest, user);

            assertThat(response).isNotNull();
            assertThat(response.getAchieved()).isTrue();
        }
    }

    @Nested
    @DisplayName("Goal completion tests")
    class GoalCompletionTests {

        @Test
        @DisplayName("Should mark goal as completed when target is reached")
        void shouldMarkGoalAsCompleted() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(progressEntryMapper.toEntity(progressRequest)).thenReturn(progressEntry);
            when(progressEntryRepository.save(any(ProgressEntry.class))).thenReturn(progressEntry);
            when(progressEntryMapper.toResponse(progressEntry)).thenReturn(progressResponse);
            when(progressEntryRepository.sumValueByGoal(goal)).thenReturn(BigDecimal.valueOf(5));
            when(milestoneRepository.findByGoalAndAchievedFalseOrderByPercentageAsc(goal)).thenReturn(Collections.emptyList());

            progressService.addProgress(1L, progressRequest, user);

            verify(goalRepository, times(2)).save(argThat(g ->
                    g.getGoalStatus() == GoalStatus.COMPLETED
            ));
        }

        @Test
        @DisplayName("Should achieve milestones when progress is added")
        void shouldAchieveMilestones() {
            var milestone = Milestone.builder()
                    .id(1L)
                    .goal(goal)
                    .percentage(25)
                    .achieved(false)
                    .build();

            goal.setCurrentProgress(BigDecimal.valueOf(2));

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(progressEntryMapper.toEntity(progressRequest)).thenReturn(progressEntry);
            when(progressEntryRepository.save(any(ProgressEntry.class))).thenReturn(progressEntry);
            when(progressEntryMapper.toResponse(progressEntry)).thenReturn(progressResponse);
            when(progressEntryRepository.sumValueByGoal(goal)).thenReturn(BigDecimal.valueOf(2));
            when(milestoneRepository.findByGoalAndAchievedFalseOrderByPercentageAsc(goal)).thenReturn(List.of(milestone));

            progressService.addProgress(1L, progressRequest, user);

            verify(milestoneRepository).save(argThat(m -> m.getAchieved() && m.getAchievedAt() != null));
        }

        @Test
        @DisplayName("Should revert milestones when progress is deleted")
        void shouldRevertMilestones() {
            var milestone = Milestone.builder()
                    .id(1L)
                    .goal(goal)
                    .percentage(50)
                    .achieved(true)
                    .achievedAt(LocalDateTime.now())
                    .build();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(progressEntryRepository.findById(1L)).thenReturn(Optional.of(progressEntry));
            when(progressEntryRepository.sumValueByGoal(goal)).thenReturn(BigDecimal.ZERO);
            when(milestoneRepository.findByGoalOrderByPercentageAsc(goal)).thenReturn(List.of(milestone));

            progressService.deleteProgressEntry(1L, 1L, user);

            verify(milestoneRepository).save(argThat(m -> !m.getAchieved() && m.getAchievedAt() == null));
        }
    }

    @Nested
    @DisplayName("Delete progress edge cases")
    class DeleteProgressEdgeCases {

        @Test
        @DisplayName("Should throw exception when entry belongs to different goal")
        void shouldThrowWhenEntryBelongsToDifferentGoal() {
            var otherGoal = Goal.builder().id(99L).build();
            var otherEntry = ProgressEntry.builder()
                    .id(1L)
                    .goal(otherGoal)
                    .progressValue(BigDecimal.ONE)
                    .build();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(progressEntryRepository.findById(1L)).thenReturn(Optional.of(otherEntry));

            assertThatThrownBy(() -> progressService.deleteProgressEntry(1L, 1L, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.PROGRESS_ENTRY_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Create default milestones tests")
    class CreateDefaultMilestonesTests {

        @Test
        @DisplayName("Should create default milestones")
        void shouldCreateDefaultMilestones() {
            progressService.createDefaultMilestones(goal);

            verify(milestoneRepository, times(4)).save(any(Milestone.class));
        }
    }
}
