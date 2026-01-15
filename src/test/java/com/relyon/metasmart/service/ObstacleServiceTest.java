package com.relyon.metasmart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.obstacle.ObstacleEntry;
import com.relyon.metasmart.entity.obstacle.dto.ObstacleEntryRequest;
import com.relyon.metasmart.entity.obstacle.dto.ObstacleEntryResponse;
import com.relyon.metasmart.entity.obstacle.dto.UpdateObstacleEntryRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.mapper.ObstacleEntryMapper;
import com.relyon.metasmart.repository.GoalRepository;
import com.relyon.metasmart.repository.ObstacleEntryRepository;
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
class ObstacleServiceTest {

    @Mock
    private ObstacleEntryRepository obstacleEntryRepository;

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private ObstacleEntryMapper obstacleEntryMapper;

    @InjectMocks
    private ObstacleService obstacleService;

    private User user;
    private Goal goal;
    private ObstacleEntry obstacleEntry;
    private ObstacleEntryRequest request;
    private ObstacleEntryResponse response;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("John").email("john@test.com").build();
        goal = Goal.builder().id(1L).title("Run 5km").owner(user).build();

        obstacleEntry = ObstacleEntry.builder()
                .id(1L)
                .goal(goal)
                .entryDate(LocalDate.now())
                .obstacle("Knee pain")
                .solution("Stretching before running")
                .resolved(false)
                .build();

        request = ObstacleEntryRequest.builder()
                .obstacle("Knee pain")
                .solution("Stretching before running")
                .build();

        response = ObstacleEntryResponse.builder()
                .id(1L)
                .entryDate(LocalDate.now())
                .obstacle("Knee pain")
                .solution("Stretching before running")
                .resolved(false)
                .build();
    }

    @Nested
    @DisplayName("Create obstacle tests")
    class CreateTests {

        @Test
        @DisplayName("Should create obstacle entry successfully")
        void shouldCreateObstacleSuccessfully() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(obstacleEntryMapper.toEntity(request)).thenReturn(obstacleEntry);
            when(obstacleEntryRepository.save(any(ObstacleEntry.class))).thenReturn(obstacleEntry);
            when(obstacleEntryMapper.toResponse(obstacleEntry)).thenReturn(response);

            var result = obstacleService.create(1L, request, user);

            assertThat(result).isNotNull();
            assertThat(result.getObstacle()).isEqualTo("Knee pain");
            verify(obstacleEntryRepository).save(any(ObstacleEntry.class));
        }

        @Test
        @DisplayName("Should set entry date to today if not provided")
        void shouldSetEntryDateToTodayIfNotProvided() {
            var entryWithoutDate = ObstacleEntry.builder()
                    .id(1L)
                    .goal(goal)
                    .obstacle("Knee pain")
                    .build();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(obstacleEntryMapper.toEntity(request)).thenReturn(entryWithoutDate);
            when(obstacleEntryRepository.save(any(ObstacleEntry.class))).thenReturn(entryWithoutDate);
            when(obstacleEntryMapper.toResponse(any())).thenReturn(response);

            obstacleService.create(1L, request, user);

            verify(obstacleEntryRepository).save(argThat(entry -> entry.getEntryDate() != null));
        }

        @Test
        @DisplayName("Should throw exception when goal not found")
        void shouldThrowExceptionWhenGoalNotFound() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> obstacleService.create(1L, request, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.GOAL_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Find obstacle tests")
    class FindTests {

        @Test
        @DisplayName("Should find obstacles by goal")
        void shouldFindObstaclesByGoal() {
            var pageable = Pageable.unpaged();
            var entries = new PageImpl<>(List.of(obstacleEntry));

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(obstacleEntryRepository.findByGoalOrderByEntryDateDesc(goal, pageable)).thenReturn(entries);
            when(obstacleEntryMapper.toResponse(obstacleEntry)).thenReturn(response);

            var result = obstacleService.findByGoal(1L, user, pageable);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should find obstacles by date range")
        void shouldFindObstaclesByDateRange() {
            var pageable = Pageable.unpaged();
            var startDate = LocalDate.now().minusDays(7);
            var endDate = LocalDate.now();
            var entries = new PageImpl<>(List.of(obstacleEntry));

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(obstacleEntryRepository.findByGoalAndEntryDateBetweenOrderByEntryDateDesc(
                    goal, startDate, endDate, pageable)).thenReturn(entries);
            when(obstacleEntryMapper.toResponse(obstacleEntry)).thenReturn(response);

            var result = obstacleService.findByGoalAndDateRange(1L, user, startDate, endDate, pageable);

            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Update obstacle tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update obstacle entry successfully")
        void shouldUpdateObstacleSuccessfully() {
            var updateRequest = UpdateObstacleEntryRequest.builder()
                    .solution("New solution")
                    .resolved(true)
                    .build();

            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(obstacleEntryRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.of(obstacleEntry));
            when(obstacleEntryRepository.save(any(ObstacleEntry.class))).thenReturn(obstacleEntry);
            when(obstacleEntryMapper.toResponse(obstacleEntry)).thenReturn(response);

            var result = obstacleService.update(1L, 1L, updateRequest, user);

            assertThat(result).isNotNull();
            verify(obstacleEntryRepository).save(any(ObstacleEntry.class));
        }

        @Test
        @DisplayName("Should throw exception when obstacle not found")
        void shouldThrowExceptionWhenObstacleNotFound() {
            var updateRequest = UpdateObstacleEntryRequest.builder().build();
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(obstacleEntryRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> obstacleService.update(1L, 1L, updateRequest, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.OBSTACLE_ENTRY_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Delete obstacle tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete obstacle entry successfully")
        void shouldDeleteObstacleSuccessfully() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(obstacleEntryRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.of(obstacleEntry));

            obstacleService.delete(1L, 1L, user);

            verify(obstacleEntryRepository).delete(obstacleEntry);
        }

        @Test
        @DisplayName("Should throw exception when obstacle not found")
        void shouldThrowExceptionWhenDeletingNonExistentObstacle() {
            when(goalRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(goal));
            when(obstacleEntryRepository.findByIdAndGoal(1L, goal)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> obstacleService.delete(1L, 1L, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.OBSTACLE_ENTRY_NOT_FOUND);
        }
    }
}
