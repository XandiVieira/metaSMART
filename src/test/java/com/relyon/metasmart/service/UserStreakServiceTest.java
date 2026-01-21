package com.relyon.metasmart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.relyon.metasmart.entity.actionplan.CompletionStatus;
import com.relyon.metasmart.entity.journal.DailyJournal;
import com.relyon.metasmart.entity.streak.StreakInfo;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.repository.DailyJournalRepository;
import com.relyon.metasmart.repository.ProgressEntryRepository;
import com.relyon.metasmart.repository.StreakInfoRepository;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserStreakServiceTest {

    @Mock
    private StreakInfoRepository streakInfoRepository;

    @Mock
    private TaskCompletionRepository taskCompletionRepository;

    @Mock
    private ProgressEntryRepository progressEntryRepository;

    @Mock
    private DailyJournalRepository dailyJournalRepository;

    @Mock
    private UserProfileService userProfileService;

    @InjectMocks
    private UserStreakService userStreakService;

    private User user;
    private StreakInfo streakInfo;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("John")
                .email("john@test.com")
                .streakShields(1)
                .build();

        streakInfo = StreakInfo.builder()
                .id(1L)
                .user(user)
                .currentMaintainedStreak(5)
                .bestMaintainedStreak(10)
                .currentPerfectStreak(3)
                .bestPerfectStreak(7)
                .lastUpdatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Get user streak tests")
    class GetUserStreakTests {

        @Test
        @DisplayName("Should return user streak with existing streak info")
        void shouldReturnUserStreakWithExistingStreakInfo() {
            var today = LocalDate.now();
            when(streakInfoRepository.findByUserAndGoalIsNullAndActionItemIsNull(user))
                    .thenReturn(Optional.of(streakInfo));
            when(dailyJournalRepository.countShieldsUsedInWeek(eq(user), any(), any())).thenReturn(0);
            when(dailyJournalRepository.countByUserAndMonth(eq(user), any(), any())).thenReturn(15L);

            var response = userStreakService.getUserStreak(user);

            assertThat(response.getCurrentStreak()).isEqualTo(5);
            assertThat(response.getBestStreak()).isEqualTo(10);
            assertThat(response.getShieldsAvailable()).isEqualTo(1);
            assertThat(response.getJournalEntriesThisMonth()).isEqualTo(15L);
        }

        @Test
        @DisplayName("Should return default streak when no streak info exists")
        void shouldReturnDefaultStreakWhenNoStreakInfoExists() {
            when(streakInfoRepository.findByUserAndGoalIsNullAndActionItemIsNull(user))
                    .thenReturn(Optional.empty());
            when(dailyJournalRepository.countShieldsUsedInWeek(eq(user), any(), any())).thenReturn(0);
            when(dailyJournalRepository.countByUserAndMonth(eq(user), any(), any())).thenReturn(0L);

            var response = userStreakService.getUserStreak(user);

            assertThat(response.getCurrentStreak()).isZero();
            assertThat(response.getBestStreak()).isZero();
        }

        @Test
        @DisplayName("Should limit shields available based on max shields and used this week")
        void shouldLimitShieldsAvailableBasedOnMaxAndUsedThisWeek() {
            user.setStreakShields(5);
            when(streakInfoRepository.findByUserAndGoalIsNullAndActionItemIsNull(user))
                    .thenReturn(Optional.of(streakInfo));
            when(dailyJournalRepository.countShieldsUsedInWeek(eq(user), any(), any())).thenReturn(1);
            when(dailyJournalRepository.countByUserAndMonth(eq(user), any(), any())).thenReturn(0L);

            var response = userStreakService.getUserStreak(user);

            assertThat(response.getShieldsAvailable()).isEqualTo(1);
            assertThat(response.getShieldsUsedThisWeek()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Process end of day tests")
    class ProcessEndOfDayTests {

        @Test
        @DisplayName("Should increment streak when user has real activity")
        void shouldIncrementStreakWhenUserHasRealActivity() {
            var today = LocalDate.now();
            when(taskCompletionRepository.hasCompletedTaskOnDate(eq(user), any(), eq(today))).thenReturn(true);
            when(dailyJournalRepository.existsByUserAndJournalDate(user, today)).thenReturn(false);
            when(streakInfoRepository.findByUserAndGoalIsNullAndActionItemIsNull(user))
                    .thenReturn(Optional.of(streakInfo));

            userStreakService.processEndOfDay(user, today);

            assertThat(streakInfo.getCurrentMaintainedStreak()).isEqualTo(6);
            verify(streakInfoRepository).save(streakInfo);
        }

        @Test
        @DisplayName("Should use shield when no real activity but has journal")
        void shouldUseShieldWhenNoRealActivityButHasJournal() {
            var today = LocalDate.now();
            var journal = DailyJournal.builder()
                    .id(1L)
                    .user(user)
                    .journalDate(today)
                    .shieldUsed(false)
                    .build();

            when(taskCompletionRepository.hasCompletedTaskOnDate(eq(user), any(), eq(today))).thenReturn(false);
            when(progressEntryRepository.hasProgressOnDate(user, today)).thenReturn(false);
            when(dailyJournalRepository.existsByUserAndJournalDate(user, today)).thenReturn(true);
            when(streakInfoRepository.findByUserAndGoalIsNullAndActionItemIsNull(user))
                    .thenReturn(Optional.of(streakInfo));
            when(dailyJournalRepository.countShieldsUsedInWeek(eq(user), any(), any())).thenReturn(0);
            when(dailyJournalRepository.findByUserAndJournalDate(user, today)).thenReturn(Optional.of(journal));

            userStreakService.processEndOfDay(user, today);

            verify(userProfileService).useStreakShield(user, 1);
            assertThat(journal.getShieldUsed()).isTrue();
            verify(streakInfoRepository).save(streakInfo);
        }

        @Test
        @DisplayName("Should reset streak when no activity and no shield available")
        void shouldResetStreakWhenNoActivityAndNoShieldAvailable() {
            var today = LocalDate.now();
            user.setStreakShields(0);

            when(taskCompletionRepository.hasCompletedTaskOnDate(eq(user), any(), eq(today))).thenReturn(false);
            when(progressEntryRepository.hasProgressOnDate(user, today)).thenReturn(false);
            when(dailyJournalRepository.existsByUserAndJournalDate(user, today)).thenReturn(false);
            when(streakInfoRepository.findByUserAndGoalIsNullAndActionItemIsNull(user))
                    .thenReturn(Optional.of(streakInfo));

            userStreakService.processEndOfDay(user, today);

            assertThat(streakInfo.getCurrentMaintainedStreak()).isZero();
            verify(streakInfoRepository).save(streakInfo);
        }

        @Test
        @DisplayName("Should create default streak info if none exists")
        void shouldCreateDefaultStreakInfoIfNoneExists() {
            var today = LocalDate.now();
            var newStreakInfo = StreakInfo.builder()
                    .id(2L)
                    .user(user)
                    .currentMaintainedStreak(0)
                    .bestMaintainedStreak(0)
                    .build();

            when(taskCompletionRepository.hasCompletedTaskOnDate(eq(user), any(), eq(today))).thenReturn(true);
            when(dailyJournalRepository.existsByUserAndJournalDate(user, today)).thenReturn(false);
            when(streakInfoRepository.findByUserAndGoalIsNullAndActionItemIsNull(user))
                    .thenReturn(Optional.empty());
            when(streakInfoRepository.save(any(StreakInfo.class))).thenReturn(newStreakInfo);

            userStreakService.processEndOfDay(user, today);

            verify(streakInfoRepository, times(2)).save(any(StreakInfo.class));
        }
    }

    @Nested
    @DisplayName("Recalculate streak tests")
    class RecalculateStreakTests {

        @Test
        @DisplayName("Should recalculate streak based on activity history")
        void shouldRecalculateStreakBasedOnActivityHistory() {
            var today = LocalDate.now();
            when(streakInfoRepository.findByUserAndGoalIsNullAndActionItemIsNull(user))
                    .thenReturn(Optional.of(streakInfo));
            when(taskCompletionRepository.hasCompletedTaskOnDate(eq(user), any(), eq(today))).thenReturn(true);
            when(taskCompletionRepository.hasCompletedTaskOnDate(eq(user), any(), eq(today.minusDays(1)))).thenReturn(true);
            when(taskCompletionRepository.hasCompletedTaskOnDate(eq(user), any(), eq(today.minusDays(2)))).thenReturn(false);
            when(progressEntryRepository.hasProgressOnDate(user, today.minusDays(2))).thenReturn(false);
            when(dailyJournalRepository.existsByUserAndJournalDate(user, today)).thenReturn(false);
            when(dailyJournalRepository.existsByUserAndJournalDate(user, today.minusDays(1))).thenReturn(false);
            when(dailyJournalRepository.existsByUserAndJournalDate(user, today.minusDays(2))).thenReturn(false);
            when(dailyJournalRepository.findByUserAndJournalDate(eq(user), any())).thenReturn(Optional.empty());

            userStreakService.recalculateStreak(user);

            assertThat(streakInfo.getCurrentMaintainedStreak()).isEqualTo(2);
            verify(streakInfoRepository).save(streakInfo);
        }

        @Test
        @DisplayName("Should update best streak when current exceeds it")
        void shouldUpdateBestStreakWhenCurrentExceedsIt() {
            var today = LocalDate.now();
            streakInfo.setBestMaintainedStreak(1);
            when(streakInfoRepository.findByUserAndGoalIsNullAndActionItemIsNull(user))
                    .thenReturn(Optional.of(streakInfo));
            when(taskCompletionRepository.hasCompletedTaskOnDate(eq(user), any(), eq(today))).thenReturn(true);
            when(taskCompletionRepository.hasCompletedTaskOnDate(eq(user), any(), eq(today.minusDays(1)))).thenReturn(true);
            when(taskCompletionRepository.hasCompletedTaskOnDate(eq(user), any(), eq(today.minusDays(2)))).thenReturn(true);
            when(taskCompletionRepository.hasCompletedTaskOnDate(eq(user), any(), eq(today.minusDays(3)))).thenReturn(false);
            when(progressEntryRepository.hasProgressOnDate(user, today.minusDays(3))).thenReturn(false);
            when(dailyJournalRepository.existsByUserAndJournalDate(eq(user), any())).thenReturn(false);
            when(dailyJournalRepository.findByUserAndJournalDate(eq(user), any())).thenReturn(Optional.empty());

            userStreakService.recalculateStreak(user);

            assertThat(streakInfo.getCurrentMaintainedStreak()).isEqualTo(3);
            assertThat(streakInfo.getBestMaintainedStreak()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("On activity recorded tests")
    class OnActivityRecordedTests {

        @Test
        @DisplayName("Should increment streak when activity recorded today and last was yesterday")
        void shouldIncrementStreakWhenActivityRecordedTodayAndLastWasYesterday() {
            streakInfo.setLastUpdatedAt(LocalDateTime.now().minusDays(1));
            when(streakInfoRepository.findByUserAndGoalIsNullAndActionItemIsNull(user))
                    .thenReturn(Optional.of(streakInfo));

            userStreakService.onActivityRecorded(user);

            assertThat(streakInfo.getCurrentMaintainedStreak()).isEqualTo(6);
            verify(streakInfoRepository).save(streakInfo);
        }

        @Test
        @DisplayName("Should recalculate streak when last activity was more than a day ago")
        void shouldRecalculateStreakWhenLastActivityWasMoreThanADayAgo() {
            var today = LocalDate.now();
            streakInfo.setLastUpdatedAt(LocalDateTime.now().minusDays(3));
            when(streakInfoRepository.findByUserAndGoalIsNullAndActionItemIsNull(user))
                    .thenReturn(Optional.of(streakInfo));
            when(taskCompletionRepository.hasCompletedTaskOnDate(eq(user), any(), eq(today))).thenReturn(true);
            when(taskCompletionRepository.hasCompletedTaskOnDate(eq(user), any(), eq(today.minusDays(1)))).thenReturn(false);
            when(progressEntryRepository.hasProgressOnDate(user, today.minusDays(1))).thenReturn(false);
            when(dailyJournalRepository.existsByUserAndJournalDate(eq(user), any())).thenReturn(false);
            when(dailyJournalRepository.findByUserAndJournalDate(eq(user), any())).thenReturn(Optional.empty());

            userStreakService.onActivityRecorded(user);

            verify(streakInfoRepository).save(streakInfo);
        }
    }

    @Nested
    @DisplayName("Check and award journal shield tests")
    class CheckAndAwardJournalShieldTests {

        @Test
        @DisplayName("Should award shield when user has 7 consecutive journal days")
        void shouldAwardShieldWhenUserHas7ConsecutiveJournalDays() {
            var today = LocalDate.now();
            var journalDates = List.of(
                    today.minusDays(6), today.minusDays(5), today.minusDays(4),
                    today.minusDays(3), today.minusDays(2), today.minusDays(1), today
            );

            user.setStreakShields(0);
            when(dailyJournalRepository.findJournalDatesByUserAndDateRange(eq(user), any(), any()))
                    .thenReturn(journalDates);

            userStreakService.checkAndAwardJournalShield(user, today);

            verify(userProfileService).addStreakShield(user, 1);
        }

        @Test
        @DisplayName("Should not award shield when user already has max shields")
        void shouldNotAwardShieldWhenUserAlreadyHasMaxShields() {
            var today = LocalDate.now();
            user.setStreakShields(2);

            userStreakService.checkAndAwardJournalShield(user, today);

            verify(userProfileService, never()).addStreakShield(any(), anyInt());
            verify(dailyJournalRepository, never()).findJournalDatesByUserAndDateRange(any(), any(), any());
        }

        @Test
        @DisplayName("Should not award shield when user has less than 7 consecutive days")
        void shouldNotAwardShieldWhenUserHasLessThan7ConsecutiveDays() {
            var today = LocalDate.now();
            var journalDates = List.of(
                    today.minusDays(5), today.minusDays(4),
                    today.minusDays(2), today.minusDays(1), today
            );

            user.setStreakShields(0);
            when(dailyJournalRepository.findJournalDatesByUserAndDateRange(eq(user), any(), any()))
                    .thenReturn(journalDates);

            userStreakService.checkAndAwardJournalShield(user, today);

            verify(userProfileService, never()).addStreakShield(any(), anyInt());
        }

        @Test
        @DisplayName("Should not award shield when there is a gap in consecutive days")
        void shouldNotAwardShieldWhenThereIsAGapInConsecutiveDays() {
            var today = LocalDate.now();
            var journalDates = List.of(
                    today.minusDays(6), today.minusDays(5), today.minusDays(4),
                    today.minusDays(2), today.minusDays(1), today
            );

            user.setStreakShields(0);
            when(dailyJournalRepository.findJournalDatesByUserAndDateRange(eq(user), any(), any()))
                    .thenReturn(journalDates);

            userStreakService.checkAndAwardJournalShield(user, today);

            verify(userProfileService, never()).addStreakShield(any(), anyInt());
        }
    }

    @Nested
    @DisplayName("Streak shield tests")
    class StreakShieldTests {

        @Test
        @DisplayName("Should not use shield when user has no shields")
        void shouldNotUseShieldWhenUserHasNoShields() {
            var today = LocalDate.now();
            user.setStreakShields(0);

            when(taskCompletionRepository.hasCompletedTaskOnDate(eq(user), any(), eq(today))).thenReturn(false);
            when(progressEntryRepository.hasProgressOnDate(user, today)).thenReturn(false);
            when(dailyJournalRepository.existsByUserAndJournalDate(user, today)).thenReturn(true);
            when(streakInfoRepository.findByUserAndGoalIsNullAndActionItemIsNull(user))
                    .thenReturn(Optional.of(streakInfo));

            userStreakService.processEndOfDay(user, today);

            verify(userProfileService, never()).useStreakShield(any(), anyInt());
            assertThat(streakInfo.getCurrentMaintainedStreak()).isZero();
        }

        @Test
        @DisplayName("Should not use shield when weekly limit reached")
        void shouldNotUseShieldWhenWeeklyLimitReached() {
            var today = LocalDate.now();
            user.setStreakShields(2);

            when(taskCompletionRepository.hasCompletedTaskOnDate(eq(user), any(), eq(today))).thenReturn(false);
            when(progressEntryRepository.hasProgressOnDate(user, today)).thenReturn(false);
            when(dailyJournalRepository.existsByUserAndJournalDate(user, today)).thenReturn(true);
            when(streakInfoRepository.findByUserAndGoalIsNullAndActionItemIsNull(user))
                    .thenReturn(Optional.of(streakInfo));
            when(dailyJournalRepository.countShieldsUsedInWeek(eq(user), any(), any())).thenReturn(1);

            userStreakService.processEndOfDay(user, today);

            verify(userProfileService, never()).useStreakShield(any(), anyInt());
            assertThat(streakInfo.getCurrentMaintainedStreak()).isZero();
        }
    }
}
