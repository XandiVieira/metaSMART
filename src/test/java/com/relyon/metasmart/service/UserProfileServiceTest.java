package com.relyon.metasmart.service;

import com.relyon.metasmart.entity.goal.GoalStatus;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.entity.user.dto.UpdateProfileRequest;
import com.relyon.metasmart.repository.GoalRepository;
import com.relyon.metasmart.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GoalRepository goalRepository;

    @InjectMocks
    private UserProfileService userProfileService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@test.com")
                .createdAt(LocalDateTime.now().minusDays(30))
                .streakShields(3)
                .build();
    }

    @Nested
    @DisplayName("Get profile tests")
    class GetProfileTests {

        @Test
        @DisplayName("Should get profile with goal counts")
        void shouldGetProfileWithGoalCounts() {
            when(goalRepository.countByOwnerAndArchivedAtIsNull(user)).thenReturn(10L);
            when(goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.COMPLETED))
                    .thenReturn(5L);

            var result = userProfileService.getProfile(user);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("John Doe");
            assertThat(result.getEmail()).isEqualTo("john@test.com");
            assertThat(result.getTotalGoals()).isEqualTo(10);
            assertThat(result.getCompletedGoals()).isEqualTo(5);
            assertThat(result.getStreakShields()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should get profile with zero goals")
        void shouldGetProfileWithZeroGoals() {
            when(goalRepository.countByOwnerAndArchivedAtIsNull(user)).thenReturn(0L);
            when(goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(user, GoalStatus.COMPLETED))
                    .thenReturn(0L);

            var result = userProfileService.getProfile(user);

            assertThat(result.getTotalGoals()).isZero();
            assertThat(result.getCompletedGoals()).isZero();
        }
    }

    @Nested
    @DisplayName("Update profile tests")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update profile name")
        void shouldUpdateProfileName() {
            var request = UpdateProfileRequest.builder()
                    .name("John Smith")
                    .build();

            when(userRepository.save(any(User.class))).thenReturn(user);
            when(goalRepository.countByOwnerAndArchivedAtIsNull(any())).thenReturn(0L);
            when(goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(any(), any())).thenReturn(0L);

            var result = userProfileService.updateProfile(user, request);

            assertThat(result).isNotNull();
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should not update when name is null")
        void shouldNotUpdateWhenNameIsNull() {
            var request = UpdateProfileRequest.builder().build();

            when(userRepository.save(any(User.class))).thenReturn(user);
            when(goalRepository.countByOwnerAndArchivedAtIsNull(any())).thenReturn(0L);
            when(goalRepository.countByOwnerAndGoalStatusAndArchivedAtIsNull(any(), any())).thenReturn(0L);

            var result = userProfileService.updateProfile(user, request);

            assertThat(result.getName()).isEqualTo("John Doe");
        }
    }

    @Nested
    @DisplayName("Streak shield tests")
    class StreakShieldTests {

        @Test
        @DisplayName("Should add streak shields")
        void shouldAddStreakShields() {
            when(userRepository.save(any(User.class))).thenReturn(user);

            userProfileService.addStreakShield(user, 2);

            verify(userRepository).save(argThat(u -> u.getStreakShields() == 5));
        }

        @Test
        @DisplayName("Should use streak shield successfully")
        void shouldUseStreakShieldSuccessfully() {
            when(userRepository.save(any(User.class))).thenReturn(user);

            var result = userProfileService.useStreakShield(user);

            assertThat(result).isTrue();
            verify(userRepository).save(argThat(u -> u.getStreakShields() == 2));
        }

        @Test
        @DisplayName("Should return false when no shields available")
        void shouldReturnFalseWhenNoShieldsAvailable() {
            user.setStreakShields(0);

            var result = userProfileService.useStreakShield(user);

            assertThat(result).isFalse();
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should handle exactly one shield")
        void shouldHandleExactlyOneShield() {
            user.setStreakShields(1);
            when(userRepository.save(any(User.class))).thenReturn(user);

            var result = userProfileService.useStreakShield(user);

            assertThat(result).isTrue();
            verify(userRepository).save(argThat(u -> u.getStreakShields() == 0));
        }
    }
}
