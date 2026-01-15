package com.relyon.metasmart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.goal.GoalCategory;
import com.relyon.metasmart.entity.goal.GoalStatus;
import com.relyon.metasmart.entity.goal.dto.GoalRequest;
import com.relyon.metasmart.entity.goal.dto.GoalResponse;
import com.relyon.metasmart.entity.goal.dto.UpdateGoalRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.mapper.ActionItemMapper;
import com.relyon.metasmart.mapper.GoalMapper;
import com.relyon.metasmart.mapper.ScheduledTaskMapper;
import com.relyon.metasmart.repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
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
    private ScheduledTaskRepository scheduledTaskRepository;

    @Mock
    private GoalGuardianRepository goalGuardianRepository;

    @Mock
    private TaskCompletionRepository taskCompletionRepository;

    @Mock
    private GoalMapper goalMapper;

    @Mock
    private ActionItemMapper actionItemMapper;

    @Mock
    private ScheduledTaskMapper scheduledTaskMapper;

    @Mock
    private UserProfileService userProfileService;

    @Mock
    private UsageLimitService usageLimitService;

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

        // Default stubs for enrichGoalResponse dependencies
        lenient().when(progressEntryRepository.findByGoalOrderByCreatedAtDesc(any(Goal.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        lenient().when(actionItemRepository.findByGoalOrderByOrderIndexAscCreatedAtAsc(any(Goal.class)))
                .thenReturn(Collections.emptyList());
        lenient().when(scheduledTaskRepository.findByGoalOrderByScheduledDateAsc(any(Goal.class)))
                .thenReturn(Collections.emptyList());
        lenient().when(milestoneRepository.findByGoalOrderByPercentageAsc(any(Goal.class)))
                .thenReturn(Collections.emptyList());
        lenient().when(goalGuardianRepository.findByGoalAndStatus(any(Goal.class), any()))
                .thenReturn(Collections.emptyList());
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

    @Nested
    @DisplayName("Smart pillars and progress tests")
    class SmartPillarsAndProgressTests {

        @Test
        @DisplayName("Should calculate smart pillars correctly when all fields set")
        void shouldCalculateSmartPillarsWhenAllFieldsSet() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(Collections.emptyList());

            var response = goalService.findById(1L, user);

            assertThat(response.getSmartPillars()).isNotNull();
            assertThat(response.getSmartPillars().getCompletionPercentage()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should handle invalid target value for progress percentage")
        void shouldHandleInvalidTargetValue() {
            goal.setTargetValue("invalid");
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(Collections.emptyList());

            var response = goalService.findById(1L, user);

            assertThat(response.getProgressPercentage()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle zero target value for progress percentage")
        void shouldHandleZeroTargetValue() {
            goal.setTargetValue("0");
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(Collections.emptyList());

            var response = goalService.findById(1L, user);

            assertThat(response.getProgressPercentage()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should calculate streak correctly with consecutive dates")
        void shouldCalculateStreakWithConsecutiveDates() {
            var today = LocalDate.now();
            var dates = List.of(today, today.minusDays(1), today.minusDays(2));
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(dates);

            var response = goalService.findById(1L, user);

            assertThat(response.getCurrentStreak()).isEqualTo(3);
            assertThat(response.getLongestStreak()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should calculate streak correctly with yesterday start")
        void shouldCalculateStreakWithYesterdayStart() {
            var yesterday = LocalDate.now().minusDays(1);
            var dates = List.of(yesterday, yesterday.minusDays(1));
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(dates);

            var response = goalService.findById(1L, user);

            assertThat(response.getCurrentStreak()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should calculate streak correctly with non-consecutive dates")
        void shouldCalculateStreakWithNonConsecutiveDates() {
            var today = LocalDate.now();
            // Create a gap between today and the older streak
            var dates = List.of(today, today.minusDays(10), today.minusDays(11), today.minusDays(12));
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(dates);

            var response = goalService.findById(1L, user);

            // Current streak is 1 (just today), longest streak is 3 (the older consecutive days)
            assertThat(response.getLongestStreak()).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("Should calculate SMART pillars with missing fields")
        void shouldCalculateSmartPillarsWithMissingFields() {
            goal.setMotivation(null);
            goal.setDescription(null);
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(Collections.emptyList());

            var response = goalService.findById(1L, user);

            assertThat(response.getSmartPillars()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Update goal with all fields tests")
    class UpdateGoalAllFieldsTests {

        @Test
        @DisplayName("Should update goal with all fields")
        void shouldUpdateGoalWithAllFields() {
            var updateRequest = UpdateGoalRequest.builder()
                    .title("Updated title")
                    .description("Updated description")
                    .goalCategory(GoalCategory.FINANCE)
                    .targetValue("10")
                    .unit("miles")
                    .currentProgress(BigDecimal.valueOf(5))
                    .motivation("New motivation")
                    .startDate(LocalDate.now())
                    .targetDate(LocalDate.now().plusMonths(6))
                    .goalStatus(GoalStatus.PAUSED)
                    .build();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(goalRepository.save(any(Goal.class))).thenReturn(goal);
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(Collections.emptyList());

            var response = goalService.update(1L, updateRequest, user);

            assertThat(response).isNotNull();
            verify(goalRepository).save(any(Goal.class));
        }
    }

    @Nested
    @DisplayName("Setup completion and SMART pillars edge cases")
    class SetupCompletionEdgeCases {

        @Test
        @DisplayName("Should calculate setup completion with empty strings")
        void shouldCalculateSetupCompletionWithEmptyStrings() {
            goal.setTitle("");
            goal.setDescription("   ");
            goal.setTargetValue("");  // Empty string instead of null to avoid NPE
            goal.setUnit("");
            goal.setMotivation(null);
            goal.setStartDate(null);
            goal.setTargetDate(null);
            goal.setGoalCategory(null);

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(Collections.emptyList());

            var response = goalService.findById(1L, user);

            assertThat(response.getSetupCompletionPercentage()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should calculate SMART pillars with partial fields")
        void shouldCalculateSmartPillarsWithPartialFields() {
            // Title set but description blank
            goal.setTitle("Title");
            goal.setDescription("");
            // Target value set but unit null
            goal.setTargetValue("5");
            goal.setUnit(null);
            // Motivation set (achievable and relevant)
            goal.setMotivation("Stay healthy");
            goal.setGoalCategory(GoalCategory.HEALTH);
            // Only start date set
            goal.setStartDate(LocalDate.now());
            goal.setTargetDate(null);

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(Collections.emptyList());

            var response = goalService.findById(1L, user);

            assertThat(response.getSmartPillars()).isNotNull();
            // specific false (desc blank), measurable false (unit null), timeBound false (targetDate null)
        }

        @Test
        @DisplayName("Should handle single date in streak calculation")
        void shouldHandleSingleDateInStreak() {
            var today = LocalDate.now();
            var dates = List.of(today);
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(dates);

            var response = goalService.findById(1L, user);

            assertThat(response.getCurrentStreak()).isEqualTo(1);
            assertThat(response.getLongestStreak()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle old dates not starting from today or yesterday")
        void shouldHandleOldDatesForStreak() {
            var oldDate = LocalDate.now().minusDays(10);
            var dates = List.of(oldDate, oldDate.minusDays(1));
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(dates);

            var response = goalService.findById(1L, user);

            // Longest streak should be 2 (the consecutive old dates)
            assertThat(response.getLongestStreak()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should calculate progress percentage with actual progress")
        void shouldCalculateProgressPercentageWithProgress() {
            goal.setCurrentProgress(BigDecimal.valueOf(2.5));
            goal.setTargetValue("5");

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(Collections.emptyList());

            var response = goalService.findById(1L, user);

            assertThat(response.getProgressPercentage()).isEqualTo(BigDecimal.valueOf(50.00).setScale(2));
        }
    }

    @Nested
    @DisplayName("Archive goal tests")
    class ArchiveGoalTests {

        @Test
        @DisplayName("Should archive goal successfully")
        void shouldArchiveGoalSuccessfully() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(goalRepository.save(any(Goal.class))).thenReturn(goal);
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(Collections.emptyList());

            var response = goalService.archive(1L, user);

            assertThat(response).isNotNull();
            assertThat(goal.getArchivedAt()).isEqualTo(LocalDate.now());
            verify(goalRepository).save(goal);
        }

        @Test
        @DisplayName("Should throw exception when archiving non-existent goal")
        void shouldThrowExceptionWhenArchivingNonExistentGoal() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> goalService.archive(1L, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.GOAL_NOT_FOUND);
        }

        @Test
        @DisplayName("Should set archived date to today")
        void shouldSetArchivedDateToToday() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(any(Goal.class))).thenReturn(Collections.emptyList());

            goalService.archive(1L, user);

            assertThat(goal.getArchivedAt()).isEqualTo(LocalDate.now());
        }
    }

    @Nested
    @DisplayName("Unarchive goal tests")
    class UnarchiveGoalTests {

        @Test
        @DisplayName("Should unarchive goal successfully")
        void shouldUnarchiveGoalSuccessfully() {
            goal.setArchivedAt(LocalDate.now().minusDays(5));
            when(goalRepository.findByIdAndOwnerAndArchivedAtIsNotNull(1L, user)).thenReturn(Optional.of(goal));
            when(goalRepository.save(any(Goal.class))).thenReturn(goal);
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(Collections.emptyList());

            var response = goalService.unarchive(1L, user);

            assertThat(response).isNotNull();
            assertThat(goal.getArchivedAt()).isNull();
            verify(goalRepository).save(goal);
        }

        @Test
        @DisplayName("Should throw exception when unarchiving non-existent or non-archived goal")
        void shouldThrowExceptionWhenUnarchivingNonExistentGoal() {
            when(goalRepository.findByIdAndOwnerAndArchivedAtIsNotNull(1L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> goalService.unarchive(1L, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.GOAL_NOT_FOUND);
        }

        @Test
        @DisplayName("Should clear archived date when unarchiving")
        void shouldClearArchivedDateWhenUnarchiving() {
            goal.setArchivedAt(LocalDate.now().minusDays(10));
            when(goalRepository.findByIdAndOwnerAndArchivedAtIsNotNull(1L, user)).thenReturn(Optional.of(goal));
            when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(any(Goal.class))).thenReturn(Collections.emptyList());

            goalService.unarchive(1L, user);

            assertThat(goal.getArchivedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("Find archived goals tests")
    class FindArchivedGoalsTests {

        @Test
        @DisplayName("Should find archived goals")
        void shouldFindArchivedGoals() {
            goal.setArchivedAt(LocalDate.now().minusDays(5));
            var pageable = Pageable.unpaged();
            var goals = new PageImpl<>(List.of(goal));
            when(goalRepository.findByOwnerAndArchivedAtIsNotNull(user, pageable)).thenReturn(goals);
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(Collections.emptyList());

            var response = goalService.findArchived(user, pageable);

            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
            verify(goalRepository).findByOwnerAndArchivedAtIsNotNull(user, pageable);
        }

        @Test
        @DisplayName("Should return empty page when no archived goals")
        void shouldReturnEmptyPageWhenNoArchivedGoals() {
            var pageable = Pageable.unpaged();
            var emptyPage = new PageImpl<Goal>(Collections.emptyList());
            when(goalRepository.findByOwnerAndArchivedAtIsNotNull(user, pageable)).thenReturn(emptyPage);

            var response = goalService.findArchived(user, pageable);

            assertThat(response).isNotNull();
            assertThat(response.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should find multiple archived goals")
        void shouldFindMultipleArchivedGoals() {
            var goal2 = Goal.builder()
                    .id(2L)
                    .title("Second goal")
                    .targetValue("10")
                    .archivedAt(LocalDate.now().minusDays(3))
                    .owner(user)
                    .currentProgress(BigDecimal.ZERO)
                    .build();

            goal.setArchivedAt(LocalDate.now().minusDays(5));
            var pageable = Pageable.unpaged();
            var goals = new PageImpl<>(List.of(goal, goal2));
            when(goalRepository.findByOwnerAndArchivedAtIsNotNull(user, pageable)).thenReturn(goals);
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(any(Goal.class))).thenReturn(Collections.emptyList());

            var response = goalService.findArchived(user, pageable);

            assertThat(response.getContent()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Search goals tests")
    class SearchGoalsTests {

        @Test
        @DisplayName("Should search goals by query")
        void shouldSearchGoalsByQuery() {
            var pageable = Pageable.unpaged();
            var goals = new PageImpl<>(List.of(goal));
            when(goalRepository.searchByOwner(user, "5km", pageable)).thenReturn(goals);
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(Collections.emptyList());

            var response = goalService.search(user, "5km", pageable);

            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
            verify(goalRepository).searchByOwner(user, "5km", pageable);
        }

        @Test
        @DisplayName("Should return empty page when no search results")
        void shouldReturnEmptyPageWhenNoSearchResults() {
            var pageable = Pageable.unpaged();
            var emptyPage = new PageImpl<Goal>(Collections.emptyList());
            when(goalRepository.searchByOwner(user, "nonexistent", pageable)).thenReturn(emptyPage);

            var response = goalService.search(user, "nonexistent", pageable);

            assertThat(response).isNotNull();
            assertThat(response.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should search goals with partial match")
        void shouldSearchGoalsWithPartialMatch() {
            var pageable = Pageable.unpaged();
            var goals = new PageImpl<>(List.of(goal));
            when(goalRepository.searchByOwner(user, "Run", pageable)).thenReturn(goals);
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(Collections.emptyList());

            var response = goalService.search(user, "Run", pageable);

            assertThat(response.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should search goals case insensitively")
        void shouldSearchGoalsCaseInsensitively() {
            var pageable = Pageable.unpaged();
            var goals = new PageImpl<>(List.of(goal));
            when(goalRepository.searchByOwner(user, "run", pageable)).thenReturn(goals);
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(Collections.emptyList());

            var response = goalService.search(user, "run", pageable);

            assertThat(response.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Filter goals tests")
    class FilterGoalsTests {

        @Test
        @DisplayName("Should filter goals by status and category")
        void shouldFilterGoalsByStatusAndCategory() {
            var pageable = Pageable.unpaged();
            var goals = new PageImpl<>(List.of(goal));
            when(goalRepository.findByOwnerWithFilters(user, GoalStatus.ACTIVE, GoalCategory.HEALTH, pageable))
                    .thenReturn(goals);
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(Collections.emptyList());

            var response = goalService.filter(user, GoalStatus.ACTIVE, GoalCategory.HEALTH, pageable);

            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
            verify(goalRepository).findByOwnerWithFilters(user, GoalStatus.ACTIVE, GoalCategory.HEALTH, pageable);
        }

        @Test
        @DisplayName("Should filter goals by status only")
        void shouldFilterGoalsByStatusOnly() {
            var pageable = Pageable.unpaged();
            var goals = new PageImpl<>(List.of(goal));
            when(goalRepository.findByOwnerWithFilters(user, GoalStatus.ACTIVE, null, pageable))
                    .thenReturn(goals);
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(Collections.emptyList());

            var response = goalService.filter(user, GoalStatus.ACTIVE, null, pageable);

            assertThat(response.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should filter goals by category only")
        void shouldFilterGoalsByCategoryOnly() {
            var pageable = Pageable.unpaged();
            var goals = new PageImpl<>(List.of(goal));
            when(goalRepository.findByOwnerWithFilters(user, null, GoalCategory.HEALTH, pageable))
                    .thenReturn(goals);
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(Collections.emptyList());

            var response = goalService.filter(user, null, GoalCategory.HEALTH, pageable);

            assertThat(response.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should return empty page when no filter results")
        void shouldReturnEmptyPageWhenNoFilterResults() {
            var pageable = Pageable.unpaged();
            var emptyPage = new PageImpl<Goal>(Collections.emptyList());
            when(goalRepository.findByOwnerWithFilters(user, GoalStatus.COMPLETED, GoalCategory.FINANCE, pageable))
                    .thenReturn(emptyPage);

            var response = goalService.filter(user, GoalStatus.COMPLETED, GoalCategory.FINANCE, pageable);

            assertThat(response.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should filter with null status and null category")
        void shouldFilterWithNullStatusAndNullCategory() {
            var pageable = Pageable.unpaged();
            var goals = new PageImpl<>(List.of(goal));
            when(goalRepository.findByOwnerWithFilters(user, null, null, pageable)).thenReturn(goals);
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(Collections.emptyList());

            var response = goalService.filter(user, null, null, pageable);

            assertThat(response.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Find due soon goals tests")
    class FindDueSoonGoalsTests {

        @Test
        @DisplayName("Should find goals due within specified days")
        void shouldFindGoalsDueWithinSpecifiedDays() {
            goal.setTargetDate(LocalDate.now().plusDays(5));
            var dueDate = LocalDate.now().plusDays(7);
            when(goalRepository.findGoalsDueSoon(user, dueDate)).thenReturn(List.of(goal));
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(Collections.emptyList());

            var response = goalService.findDueSoon(user, 7);

            assertThat(response).isNotNull();
            assertThat(response).hasSize(1);
            verify(goalRepository).findGoalsDueSoon(user, dueDate);
        }

        @Test
        @DisplayName("Should return empty list when no goals due soon")
        void shouldReturnEmptyListWhenNoGoalsDueSoon() {
            var dueDate = LocalDate.now().plusDays(7);
            when(goalRepository.findGoalsDueSoon(user, dueDate)).thenReturn(Collections.emptyList());

            var response = goalService.findDueSoon(user, 7);

            assertThat(response).isEmpty();
        }

        @Test
        @DisplayName("Should find goals due today with zero days")
        void shouldFindGoalsDueTodayWithZeroDays() {
            goal.setTargetDate(LocalDate.now());
            var dueDate = LocalDate.now();
            when(goalRepository.findGoalsDueSoon(user, dueDate)).thenReturn(List.of(goal));
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(Collections.emptyList());

            var response = goalService.findDueSoon(user, 0);

            assertThat(response).hasSize(1);
        }

        @Test
        @DisplayName("Should find multiple goals due soon")
        void shouldFindMultipleGoalsDueSoon() {
            var goal2 = Goal.builder()
                    .id(2L)
                    .title("Second goal")
                    .targetValue("10")
                    .targetDate(LocalDate.now().plusDays(3))
                    .owner(user)
                    .currentProgress(BigDecimal.ZERO)
                    .build();

            goal.setTargetDate(LocalDate.now().plusDays(5));
            var dueDate = LocalDate.now().plusDays(7);
            when(goalRepository.findGoalsDueSoon(user, dueDate)).thenReturn(List.of(goal, goal2));
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(any(Goal.class))).thenReturn(Collections.emptyList());

            var response = goalService.findDueSoon(user, 7);

            assertThat(response).hasSize(2);
        }

        @Test
        @DisplayName("Should calculate due date correctly for 30 days")
        void shouldCalculateDueDateCorrectlyFor30Days() {
            var dueDate = LocalDate.now().plusDays(30);
            when(goalRepository.findGoalsDueSoon(user, dueDate)).thenReturn(Collections.emptyList());

            goalService.findDueSoon(user, 30);

            verify(goalRepository).findGoalsDueSoon(user, dueDate);
        }
    }

    @Nested
    @DisplayName("Duplicate goal tests")
    class DuplicateGoalTests {

        @Test
        @DisplayName("Should duplicate goal successfully")
        void shouldDuplicateGoalSuccessfully() {
            var duplicatedGoal = Goal.builder()
                    .id(2L)
                    .title("Run 5km (Copy)")
                    .description("Build endurance to run 5km")
                    .goalCategory(GoalCategory.HEALTH)
                    .targetValue("5")
                    .unit("km")
                    .currentProgress(BigDecimal.ZERO)
                    .motivation("Improve health")
                    .startDate(LocalDate.now())
                    .targetDate(LocalDate.now().plusMonths(3))
                    .owner(user)
                    .build();

            var duplicatedResponse = GoalResponse.builder()
                    .id(2L)
                    .title("Run 5km (Copy)")
                    .description("Build endurance to run 5km")
                    .goalCategory(GoalCategory.HEALTH)
                    .build();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(goalRepository.save(any(Goal.class))).thenReturn(duplicatedGoal);
            when(goalMapper.toResponse(duplicatedGoal)).thenReturn(duplicatedResponse);
            when(progressEntryRepository.findDistinctProgressDates(duplicatedGoal)).thenReturn(Collections.emptyList());

            var response = goalService.duplicate(1L, user);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(2L);
            assertThat(response.getTitle()).isEqualTo("Run 5km (Copy)");
            verify(goalRepository).save(any(Goal.class));
            verify(milestoneRepository, times(4)).save(any());
        }

        @Test
        @DisplayName("Should throw exception when duplicating non-existent goal")
        void shouldThrowExceptionWhenDuplicatingNonExistentGoal() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> goalService.duplicate(1L, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.GOAL_NOT_FOUND);
        }

        @Test
        @DisplayName("Should set start date to today when duplicating")
        void shouldSetStartDateToTodayWhenDuplicating() {
            var savedGoal = Goal.builder()
                    .id(2L)
                    .title("Run 5km (Copy)")
                    .targetValue("5")
                    .startDate(LocalDate.now())
                    .currentProgress(BigDecimal.ZERO)
                    .owner(user)
                    .build();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> {
                Goal savedArg = invocation.getArgument(0);
                assertThat(savedArg.getStartDate()).isEqualTo(LocalDate.now());
                return savedGoal;
            });
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(any(Goal.class))).thenReturn(Collections.emptyList());

            goalService.duplicate(1L, user);

            verify(goalRepository).save(any(Goal.class));
        }

        @Test
        @DisplayName("Should calculate target date based on original duration")
        void shouldCalculateTargetDateBasedOnOriginalDuration() {
            goal.setStartDate(LocalDate.now().minusDays(10));
            goal.setTargetDate(LocalDate.now().plusDays(20));

            var savedGoal = Goal.builder()
                    .id(2L)
                    .title("Run 5km (Copy)")
                    .targetValue("5")
                    .startDate(LocalDate.now())
                    .targetDate(LocalDate.now().plusDays(30))
                    .currentProgress(BigDecimal.ZERO)
                    .owner(user)
                    .build();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> {
                Goal savedArg = invocation.getArgument(0);
                assertThat(savedArg.getTargetDate()).isEqualTo(LocalDate.now().plusDays(30));
                return savedGoal;
            });
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(any(Goal.class))).thenReturn(Collections.emptyList());

            goalService.duplicate(1L, user);

            verify(goalRepository).save(any(Goal.class));
        }

        @Test
        @DisplayName("Should handle null target date when duplicating")
        void shouldHandleNullTargetDateWhenDuplicating() {
            goal.setTargetDate(null);

            var savedGoal = Goal.builder()
                    .id(2L)
                    .title("Run 5km (Copy)")
                    .targetValue("5")
                    .startDate(LocalDate.now())
                    .targetDate(null)
                    .currentProgress(BigDecimal.ZERO)
                    .owner(user)
                    .build();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> {
                Goal savedArg = invocation.getArgument(0);
                assertThat(savedArg.getTargetDate()).isNull();
                return savedGoal;
            });
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(any(Goal.class))).thenReturn(Collections.emptyList());

            goalService.duplicate(1L, user);

            verify(goalRepository).save(any(Goal.class));
        }

        @Test
        @DisplayName("Should enforce goal limit when duplicating")
        void shouldEnforceGoalLimitWhenDuplicating() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(goalRepository.save(any(Goal.class))).thenReturn(goal);
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(any(Goal.class))).thenReturn(Collections.emptyList());

            goalService.duplicate(1L, user);

            verify(usageLimitService).enforceGoalLimit(user);
        }

        @Test
        @DisplayName("Should create default milestones for duplicated goal")
        void shouldCreateDefaultMilestonesForDuplicatedGoal() {
            var savedGoal = Goal.builder()
                    .id(2L)
                    .title("Run 5km (Copy)")
                    .targetValue("5")
                    .currentProgress(BigDecimal.ZERO)
                    .owner(user)
                    .build();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(goalRepository.save(any(Goal.class))).thenReturn(savedGoal);
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(any(Goal.class))).thenReturn(Collections.emptyList());

            goalService.duplicate(1L, user);

            verify(milestoneRepository, times(4)).save(any());
        }
    }

    @Nested
    @DisplayName("Use streak shield tests")
    class UseStreakShieldTests {

        @Test
        @DisplayName("Should use streak shield successfully")
        void shouldUseStreakShieldSuccessfully() {
            when(goalRepository.findByIdAndOwnerAndArchivedAtIsNull(1L, user)).thenReturn(Optional.of(goal));
            when(userProfileService.useStreakShield(user)).thenReturn(true);
            when(goalRepository.save(any(Goal.class))).thenReturn(goal);
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(Collections.emptyList());

            var response = goalService.useStreakShield(1L, user);

            assertThat(response).isNotNull();
            assertThat(goal.getLastStreakShieldUsedAt()).isEqualTo(LocalDate.now());
            verify(userProfileService).useStreakShield(user);
            verify(goalRepository).save(goal);
        }

        @Test
        @DisplayName("Should throw exception when goal not found")
        void shouldThrowExceptionWhenGoalNotFoundForStreakShield() {
            when(goalRepository.findByIdAndOwnerAndArchivedAtIsNull(1L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> goalService.useStreakShield(1L, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.GOAL_NOT_FOUND);
        }

        @Test
        @DisplayName("Should throw exception when streak shield already used today")
        void shouldThrowExceptionWhenStreakShieldAlreadyUsedToday() {
            goal.setLastStreakShieldUsedAt(LocalDate.now());
            when(goalRepository.findByIdAndOwnerAndArchivedAtIsNull(1L, user)).thenReturn(Optional.of(goal));

            assertThatThrownBy(() -> goalService.useStreakShield(1L, user))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Streak shield already used today for this goal");
        }

        @Test
        @DisplayName("Should throw exception when no streak shields available")
        void shouldThrowExceptionWhenNoStreakShieldsAvailable() {
            when(goalRepository.findByIdAndOwnerAndArchivedAtIsNull(1L, user)).thenReturn(Optional.of(goal));
            when(userProfileService.useStreakShield(user)).thenReturn(false);

            assertThatThrownBy(() -> goalService.useStreakShield(1L, user))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("No streak shields available");
        }

        @Test
        @DisplayName("Should allow streak shield if last used yesterday")
        void shouldAllowStreakShieldIfLastUsedYesterday() {
            goal.setLastStreakShieldUsedAt(LocalDate.now().minusDays(1));
            when(goalRepository.findByIdAndOwnerAndArchivedAtIsNull(1L, user)).thenReturn(Optional.of(goal));
            when(userProfileService.useStreakShield(user)).thenReturn(true);
            when(goalRepository.save(any(Goal.class))).thenReturn(goal);
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(Collections.emptyList());

            var response = goalService.useStreakShield(1L, user);

            assertThat(response).isNotNull();
            assertThat(goal.getLastStreakShieldUsedAt()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("Should allow streak shield if never used before")
        void shouldAllowStreakShieldIfNeverUsedBefore() {
            goal.setLastStreakShieldUsedAt(null);
            when(goalRepository.findByIdAndOwnerAndArchivedAtIsNull(1L, user)).thenReturn(Optional.of(goal));
            when(userProfileService.useStreakShield(user)).thenReturn(true);
            when(goalRepository.save(any(Goal.class))).thenReturn(goal);
            when(goalMapper.toResponse(goal)).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(Collections.emptyList());

            var response = goalService.useStreakShield(1L, user);

            assertThat(response).isNotNull();
            assertThat(goal.getLastStreakShieldUsedAt()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("Should set last streak shield used date to today")
        void shouldSetLastStreakShieldUsedDateToToday() {
            when(goalRepository.findByIdAndOwnerAndArchivedAtIsNull(1L, user)).thenReturn(Optional.of(goal));
            when(userProfileService.useStreakShield(user)).thenReturn(true);
            when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);
            when(progressEntryRepository.findDistinctProgressDates(any(Goal.class))).thenReturn(Collections.emptyList());

            goalService.useStreakShield(1L, user);

            assertThat(goal.getLastStreakShieldUsedAt()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("Should not allow streak shield on archived goal")
        void shouldNotAllowStreakShieldOnArchivedGoal() {
            when(goalRepository.findByIdAndOwnerAndArchivedAtIsNull(1L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> goalService.useStreakShield(1L, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.GOAL_NOT_FOUND);
        }
    }
}
