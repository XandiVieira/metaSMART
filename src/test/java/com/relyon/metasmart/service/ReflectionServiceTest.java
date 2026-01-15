package com.relyon.metasmart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.goal.GoalCategory;
import com.relyon.metasmart.entity.goal.GoalStatus;
import com.relyon.metasmart.entity.reflection.GoalReflection;
import com.relyon.metasmart.entity.reflection.ReflectionFrequency;
import com.relyon.metasmart.entity.reflection.ReflectionRating;
import com.relyon.metasmart.entity.reflection.dto.ReflectionRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.BadRequestException;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.repository.GoalReflectionRepository;
import com.relyon.metasmart.repository.GoalRepository;
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
class ReflectionServiceTest {

    @Mock
    private GoalReflectionRepository reflectionRepository;

    @Mock
    private GoalRepository goalRepository;

    @InjectMocks
    private ReflectionService reflectionService;

    private User user;
    private Goal goal;
    private GoalReflection reflection;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("John").email("john@test.com").build();
        goal = Goal.builder()
                .id(1L)
                .title("Run 5K")
                .owner(user)
                .goalStatus(GoalStatus.ACTIVE)
                .goalCategory(GoalCategory.HEALTH)
                .startDate(LocalDate.now().minusDays(41))
                .targetDate(LocalDate.now().plusDays(90))
                .createdAt(LocalDateTime.now().minusDays(41))
                .build();
        reflection = GoalReflection.builder()
                .id(1L)
                .goal(goal)
                .user(user)
                .periodStart(LocalDate.now().minusDays(7))
                .periodEnd(LocalDate.now().minusDays(1))
                .rating(ReflectionRating.GOOD)
                .wentWell("Good progress")
                .challenges("Time management")
                .adjustments("Wake up earlier")
                .moodNote("Motivated")
                .willContinue(true)
                .motivationLevel(8)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Get reflection status tests")
    class GetReflectionStatusTests {

        @Test
        @DisplayName("Should get reflection status with no previous reflections")
        void shouldGetReflectionStatusWithNoPreviousReflections() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(reflectionRepository.findFirstByGoalAndUserOrderByPeriodEndDesc(goal, user))
                    .thenReturn(Optional.empty());
            when(reflectionRepository.countByGoalAndUser(goal, user)).thenReturn(0);
            when(reflectionRepository.getAverageRating(goal, user)).thenReturn(null);

            var result = reflectionService.getReflectionStatus(1L, user);

            assertThat(result.getGoalId()).isEqualTo(1L);
            assertThat(result.getGoalTitle()).isEqualTo("Run 5K");
            assertThat(result.getFrequency()).isEqualTo(ReflectionFrequency.WEEKLY);
            assertThat(result.getTotalReflections()).isZero();
            assertThat(result.getReflectionCompleted()).isFalse();
        }

        @Test
        @DisplayName("Should get reflection status with completed reflection")
        void shouldGetReflectionStatusWithCompletedReflection() {
            reflection.setPeriodEnd(LocalDate.now());

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(reflectionRepository.findFirstByGoalAndUserOrderByPeriodEndDesc(goal, user))
                    .thenReturn(Optional.of(reflection));
            when(reflectionRepository.countByGoalAndUser(goal, user)).thenReturn(5);
            when(reflectionRepository.getAverageRating(goal, user)).thenReturn(4.2);

            var result = reflectionService.getReflectionStatus(1L, user);

            assertThat(result.getTotalReflections()).isEqualTo(5);
            assertThat(result.getAverageRating()).isEqualTo(4.2);
        }

        @Test
        @DisplayName("Should throw when goal not found")
        void shouldThrowWhenGoalNotFound() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reflectionService.getReflectionStatus(1L, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.GOAL_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Get pending reflections tests")
    class GetPendingReflectionsTests {

        @Test
        @DisplayName("Should return pending reflections")
        void shouldReturnPendingReflections() {
            when(goalRepository.findByOwnerAndGoalStatus(user, GoalStatus.ACTIVE))
                    .thenReturn(List.of(goal));
            when(reflectionRepository.findFirstByGoalAndUserOrderByPeriodEndDesc(goal, user))
                    .thenReturn(Optional.empty());

            var result = reflectionService.getPendingReflections(user);

            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("Should return empty when no pending reflections")
        void shouldReturnEmptyWhenNoPendingReflections() {
            when(goalRepository.findByOwnerAndGoalStatus(user, GoalStatus.ACTIVE))
                    .thenReturn(List.of());

            var result = reflectionService.getPendingReflections(user);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle goal with null category")
        void shouldHandleGoalWithNullCategory() {
            goal.setGoalCategory(null);

            when(goalRepository.findByOwnerAndGoalStatus(user, GoalStatus.ACTIVE))
                    .thenReturn(List.of(goal));
            when(reflectionRepository.findFirstByGoalAndUserOrderByPeriodEndDesc(goal, user))
                    .thenReturn(Optional.empty());

            var result = reflectionService.getPendingReflections(user);

            assertThat(result).isNotEmpty();
            assertThat(result.get(0).getGoalCategory()).isNull();
        }
    }

    @Nested
    @DisplayName("Create reflection tests")
    class CreateReflectionTests {

        @Test
        @DisplayName("Should create reflection successfully")
        void shouldCreateReflectionSuccessfully() {
            var request = ReflectionRequest.builder()
                    .rating(ReflectionRating.GOOD)
                    .wentWell("Made good progress")
                    .challenges("Finding time")
                    .adjustments("Schedule better")
                    .moodNote("Positive")
                    .willContinue(true)
                    .motivationLevel(8)
                    .build();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(reflectionRepository.findByGoalAndPeriod(eq(goal), eq(user), any(), any()))
                    .thenReturn(Optional.empty());
            when(reflectionRepository.save(any())).thenReturn(reflection);

            var result = reflectionService.createReflection(1L, request, user);

            assertThat(result).isNotNull();
            assertThat(result.getRating()).isEqualTo(ReflectionRating.GOOD);
            verify(reflectionRepository).save(any());
        }

        @Test
        @DisplayName("Should throw when reflection already exists for period")
        void shouldThrowWhenReflectionAlreadyExistsForPeriod() {
            var request = ReflectionRequest.builder().rating(ReflectionRating.GOOD).build();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(reflectionRepository.findByGoalAndPeriod(eq(goal), eq(user), any(), any()))
                    .thenReturn(Optional.of(reflection));

            assertThatThrownBy(() -> reflectionService.createReflection(1L, request, user))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage(ErrorMessages.REFLECTION_ALREADY_EXISTS);
        }
    }

    @Nested
    @DisplayName("Get reflection history tests")
    class GetReflectionHistoryTests {

        @Test
        @DisplayName("Should get reflection history")
        void shouldGetReflectionHistory() {
            var pageable = Pageable.ofSize(10);
            var page = new PageImpl<>(List.of(reflection));

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(reflectionRepository.findByGoalAndUserOrderByPeriodEndDesc(goal, user, pageable))
                    .thenReturn(page);

            var result = reflectionService.getReflectionHistory(1L, user, pageable);

            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Get single reflection tests")
    class GetReflectionTests {

        @Test
        @DisplayName("Should get reflection by id")
        void shouldGetReflectionById() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(reflectionRepository.findById(1L)).thenReturn(Optional.of(reflection));

            var result = reflectionService.getReflection(1L, 1L, user);

            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw when reflection not found")
        void shouldThrowWhenReflectionNotFound() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(reflectionRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reflectionService.getReflection(1L, 1L, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.REFLECTION_NOT_FOUND);
        }

        @Test
        @DisplayName("Should throw when reflection belongs to different goal")
        void shouldThrowWhenReflectionBelongsToDifferentGoal() {
            var otherGoal = Goal.builder().id(2L).build();
            reflection.setGoal(otherGoal);

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(reflectionRepository.findById(1L)).thenReturn(Optional.of(reflection));

            assertThatThrownBy(() -> reflectionService.getReflection(1L, 1L, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.REFLECTION_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Update reflection tests")
    class UpdateReflectionTests {

        @Test
        @DisplayName("Should update reflection successfully")
        void shouldUpdateReflectionSuccessfully() {
            var request = ReflectionRequest.builder()
                    .rating(ReflectionRating.EXCELLENT)
                    .wentWell("Even better")
                    .challenges("None")
                    .adjustments("Keep going")
                    .moodNote("Great")
                    .willContinue(true)
                    .motivationLevel(10)
                    .build();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(reflectionRepository.findById(1L)).thenReturn(Optional.of(reflection));
            when(reflectionRepository.save(any())).thenReturn(reflection);

            var result = reflectionService.updateReflection(1L, 1L, request, user);

            assertThat(result).isNotNull();
            verify(reflectionRepository).save(any());
        }
    }

    @Nested
    @DisplayName("Frequency calculation tests")
    class FrequencyCalculationTests {

        @Test
        @DisplayName("Should calculate every 3 days frequency for short goals")
        void shouldCalculateEvery3DaysFrequencyForShortGoals() {
            goal.setStartDate(LocalDate.now());
            goal.setTargetDate(LocalDate.now().plusDays(30));

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(reflectionRepository.findFirstByGoalAndUserOrderByPeriodEndDesc(goal, user))
                    .thenReturn(Optional.empty());
            when(reflectionRepository.countByGoalAndUser(goal, user)).thenReturn(0);
            when(reflectionRepository.getAverageRating(goal, user)).thenReturn(null);

            var result = reflectionService.getReflectionStatus(1L, user);

            assertThat(result.getFrequency()).isEqualTo(ReflectionFrequency.EVERY_3_DAYS);
        }

        @Test
        @DisplayName("Should calculate weekly frequency for medium duration goals")
        void shouldCalculateWeeklyFrequencyForMediumDurationGoals() {
            goal.setStartDate(LocalDate.now());
            goal.setTargetDate(LocalDate.now().plusDays(100));

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(reflectionRepository.findFirstByGoalAndUserOrderByPeriodEndDesc(goal, user))
                    .thenReturn(Optional.empty());
            when(reflectionRepository.countByGoalAndUser(goal, user)).thenReturn(0);
            when(reflectionRepository.getAverageRating(goal, user)).thenReturn(null);

            var result = reflectionService.getReflectionStatus(1L, user);

            assertThat(result.getFrequency()).isEqualTo(ReflectionFrequency.WEEKLY);
        }

        @Test
        @DisplayName("Should default to weekly when dates are null")
        void shouldDefaultToWeeklyWhenDatesAreNull() {
            goal.setStartDate(null);
            goal.setTargetDate(null);

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(reflectionRepository.findFirstByGoalAndUserOrderByPeriodEndDesc(goal, user))
                    .thenReturn(Optional.empty());
            when(reflectionRepository.countByGoalAndUser(goal, user)).thenReturn(0);
            when(reflectionRepository.getAverageRating(goal, user)).thenReturn(null);

            var result = reflectionService.getReflectionStatus(1L, user);

            assertThat(result.getFrequency()).isEqualTo(ReflectionFrequency.WEEKLY);
        }
    }
}
