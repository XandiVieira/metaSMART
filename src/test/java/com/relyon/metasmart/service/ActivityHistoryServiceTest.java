package com.relyon.metasmart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.relyon.metasmart.entity.actionplan.ActionItem;
import com.relyon.metasmart.entity.actionplan.CompletionStatus;
import com.relyon.metasmart.entity.actionplan.TaskCompletion;
import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.journal.DailyJournal;
import com.relyon.metasmart.entity.journal.Mood;
import com.relyon.metasmart.entity.progress.ProgressEntry;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.repository.DailyJournalRepository;
import com.relyon.metasmart.repository.ProgressEntryRepository;
import com.relyon.metasmart.repository.TaskCompletionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

@ExtendWith(MockitoExtension.class)
class ActivityHistoryServiceTest {

    @Mock
    private TaskCompletionRepository taskCompletionRepository;

    @Mock
    private ProgressEntryRepository progressEntryRepository;

    @Mock
    private DailyJournalRepository dailyJournalRepository;

    @InjectMocks
    private ActivityHistoryService activityHistoryService;

    private User user;
    private Goal goal;
    private ActionItem actionItem;
    private TaskCompletion taskCompletion;
    private ProgressEntry progressEntry;
    private DailyJournal journal;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("John")
                .email("john@test.com")
                .build();

        goal = Goal.builder()
                .id(1L)
                .title("Learn Spanish")
                .targetValue(BigDecimal.valueOf(100))
                .unit("hours")
                .owner(user)
                .build();

        actionItem = ActionItem.builder()
                .id(1L)
                .title("Study vocabulary")
                .goal(goal)
                .build();

        taskCompletion = TaskCompletion.builder()
                .id(1L)
                .actionItem(actionItem)
                .periodStart(LocalDate.now().withDayOfMonth(1))
                .scheduledDate(LocalDate.now())
                .status(CompletionStatus.COMPLETED)
                .completedAt(LocalDateTime.now())
                .note("Completed all vocab cards")
                .build();

        progressEntry = ProgressEntry.builder()
                .id(1L)
                .goal(goal)
                .progressValue(BigDecimal.valueOf(2))
                .note("Two hours of practice")
                .build();
        progressEntry.setCreatedAt(LocalDateTime.now());

        journal = DailyJournal.builder()
                .id(1L)
                .user(user)
                .journalDate(LocalDate.now())
                .content("Great progress today")
                .mood(Mood.GOOD)
                .shieldUsed(false)
                .build();
        journal.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("Get activity history tests")
    class GetActivityHistoryTests {

        @Test
        @DisplayName("Should return activity history with all activity types")
        void shouldReturnActivityHistoryWithAllActivityTypes() {
            var startDate = LocalDate.now().minusDays(7);
            var endDate = LocalDate.now();

            when(taskCompletionRepository.findByUserAndDateRange(user, startDate, endDate))
                    .thenReturn(List.of(taskCompletion));
            when(progressEntryRepository.findByUserAndDateRange(eq(user), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(List.of(progressEntry));
            when(dailyJournalRepository.findByUserAndJournalDateBetweenOrderByJournalDateDesc(user, startDate, endDate))
                    .thenReturn(List.of(journal));

            var response = activityHistoryService.getActivityHistory(user, startDate, endDate);

            assertThat(response.getStartDate()).isEqualTo(startDate);
            assertThat(response.getEndDate()).isEqualTo(endDate);
            assertThat(response.getTotalDays()).isEqualTo(8);
            assertThat(response.getActiveDays()).isPositive();
            assertThat(response.getSummary().getTotalTaskCompletions()).isEqualTo(1);
            assertThat(response.getSummary().getTotalProgressEntries()).isEqualTo(1);
            assertThat(response.getSummary().getTotalJournalEntries()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should mark days with real activity correctly")
        void shouldMarkDaysWithRealActivityCorrectly() {
            var today = LocalDate.now();

            when(taskCompletionRepository.findByUserAndDateRange(user, today, today))
                    .thenReturn(List.of(taskCompletion));
            when(progressEntryRepository.findByUserAndDateRange(eq(user), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());
            when(dailyJournalRepository.findByUserAndJournalDateBetweenOrderByJournalDateDesc(user, today, today))
                    .thenReturn(Collections.emptyList());

            var response = activityHistoryService.getActivityHistory(user, today, today);

            var dailyActivity = response.getDailyActivities().get(today);
            assertThat(dailyActivity.isHasActivity()).isTrue();
            assertThat(dailyActivity.isHasRealActivity()).isTrue();
            assertThat(dailyActivity.isProtectedByShield()).isFalse();
        }

        @Test
        @DisplayName("Should mark days with journal only as activity but not real activity")
        void shouldMarkDaysWithJournalOnlyAsActivityButNotRealActivity() {
            var today = LocalDate.now();

            when(taskCompletionRepository.findByUserAndDateRange(user, today, today))
                    .thenReturn(Collections.emptyList());
            when(progressEntryRepository.findByUserAndDateRange(eq(user), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());
            when(dailyJournalRepository.findByUserAndJournalDateBetweenOrderByJournalDateDesc(user, today, today))
                    .thenReturn(List.of(journal));

            var response = activityHistoryService.getActivityHistory(user, today, today);

            var dailyActivity = response.getDailyActivities().get(today);
            assertThat(dailyActivity.isHasActivity()).isTrue();
            assertThat(dailyActivity.isHasRealActivity()).isFalse();
            assertThat(dailyActivity.isProtectedByShield()).isFalse();
        }

        @Test
        @DisplayName("Should mark days protected by shield correctly")
        void shouldMarkDaysProtectedByShieldCorrectly() {
            var today = LocalDate.now();
            journal.setShieldUsed(true);

            when(taskCompletionRepository.findByUserAndDateRange(user, today, today))
                    .thenReturn(Collections.emptyList());
            when(progressEntryRepository.findByUserAndDateRange(eq(user), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());
            when(dailyJournalRepository.findByUserAndJournalDateBetweenOrderByJournalDateDesc(user, today, today))
                    .thenReturn(List.of(journal));

            var response = activityHistoryService.getActivityHistory(user, today, today);

            var dailyActivity = response.getDailyActivities().get(today);
            assertThat(dailyActivity.isProtectedByShield()).isTrue();
        }

        @Test
        @DisplayName("Should return empty daily activities when no activity")
        void shouldReturnEmptyDailyActivitiesWhenNoActivity() {
            var today = LocalDate.now();

            when(taskCompletionRepository.findByUserAndDateRange(user, today, today))
                    .thenReturn(Collections.emptyList());
            when(progressEntryRepository.findByUserAndDateRange(eq(user), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());
            when(dailyJournalRepository.findByUserAndJournalDateBetweenOrderByJournalDateDesc(user, today, today))
                    .thenReturn(Collections.emptyList());

            var response = activityHistoryService.getActivityHistory(user, today, today);

            var dailyActivity = response.getDailyActivities().get(today);
            assertThat(dailyActivity.isHasActivity()).isFalse();
            assertThat(dailyActivity.isHasRealActivity()).isFalse();
            assertThat(dailyActivity.getTaskCompletions()).isEmpty();
            assertThat(dailyActivity.getProgressEntries()).isEmpty();
            assertThat(dailyActivity.getJournalEntry()).isNull();
        }

        @Test
        @DisplayName("Should calculate active days correctly")
        void shouldCalculateActiveDaysCorrectly() {
            var startDate = LocalDate.now().minusDays(2);
            var endDate = LocalDate.now();

            when(taskCompletionRepository.findByUserAndDateRange(user, startDate, endDate))
                    .thenReturn(List.of(taskCompletion));
            when(progressEntryRepository.findByUserAndDateRange(eq(user), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());
            when(dailyJournalRepository.findByUserAndJournalDateBetweenOrderByJournalDateDesc(user, startDate, endDate))
                    .thenReturn(List.of(journal));

            var response = activityHistoryService.getActivityHistory(user, startDate, endDate);

            assertThat(response.getTotalDays()).isEqualTo(3);
            assertThat(response.getActiveDays()).isGreaterThanOrEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Get daily activity tests")
    class GetDailyActivityTests {

        @Test
        @DisplayName("Should return daily activity for specific date")
        void shouldReturnDailyActivityForSpecificDate() {
            var date = LocalDate.now();

            when(taskCompletionRepository.findByUserAndDateRange(user, date, date))
                    .thenReturn(List.of(taskCompletion));
            when(progressEntryRepository.findByUserAndDateRange(eq(user), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(List.of(progressEntry));
            when(dailyJournalRepository.findByUserAndJournalDate(user, date))
                    .thenReturn(Optional.of(journal));

            var response = activityHistoryService.getDailyActivity(user, date);

            assertThat(response.getDate()).isEqualTo(date);
            assertThat(response.getTaskCompletions()).hasSize(1);
            assertThat(response.getProgressEntries()).hasSize(1);
            assertThat(response.getJournalEntry()).isNotNull();
            assertThat(response.isHasActivity()).isTrue();
            assertThat(response.isHasRealActivity()).isTrue();
        }

        @Test
        @DisplayName("Should return task completion summaries with correct data")
        void shouldReturnTaskCompletionSummariesWithCorrectData() {
            var date = LocalDate.now();

            when(taskCompletionRepository.findByUserAndDateRange(user, date, date))
                    .thenReturn(List.of(taskCompletion));
            when(progressEntryRepository.findByUserAndDateRange(eq(user), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());
            when(dailyJournalRepository.findByUserAndJournalDate(user, date))
                    .thenReturn(Optional.empty());

            var response = activityHistoryService.getDailyActivity(user, date);

            var taskSummary = response.getTaskCompletions().get(0);
            assertThat(taskSummary.getId()).isEqualTo(1L);
            assertThat(taskSummary.getActionItemId()).isEqualTo(1L);
            assertThat(taskSummary.getActionItemTitle()).isEqualTo("Study vocabulary");
            assertThat(taskSummary.getGoalId()).isEqualTo(1L);
            assertThat(taskSummary.getGoalTitle()).isEqualTo("Learn Spanish");
            assertThat(taskSummary.getStatus()).isEqualTo(CompletionStatus.COMPLETED);
        }

        @Test
        @DisplayName("Should return progress entry summaries with correct data")
        void shouldReturnProgressEntrySummariesWithCorrectData() {
            var date = LocalDate.now();

            when(taskCompletionRepository.findByUserAndDateRange(user, date, date))
                    .thenReturn(Collections.emptyList());
            when(progressEntryRepository.findByUserAndDateRange(eq(user), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(List.of(progressEntry));
            when(dailyJournalRepository.findByUserAndJournalDate(user, date))
                    .thenReturn(Optional.empty());

            var response = activityHistoryService.getDailyActivity(user, date);

            var progressSummary = response.getProgressEntries().get(0);
            assertThat(progressSummary.getId()).isEqualTo(1L);
            assertThat(progressSummary.getGoalId()).isEqualTo(1L);
            assertThat(progressSummary.getGoalTitle()).isEqualTo("Learn Spanish");
            assertThat(progressSummary.getProgressValue()).isEqualTo(BigDecimal.valueOf(2));
            assertThat(progressSummary.getUnit()).isEqualTo("hours");
        }

        @Test
        @DisplayName("Should return journal entry summary with correct data")
        void shouldReturnJournalEntrySummaryWithCorrectData() {
            var date = LocalDate.now();

            when(taskCompletionRepository.findByUserAndDateRange(user, date, date))
                    .thenReturn(Collections.emptyList());
            when(progressEntryRepository.findByUserAndDateRange(eq(user), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());
            when(dailyJournalRepository.findByUserAndJournalDate(user, date))
                    .thenReturn(Optional.of(journal));

            var response = activityHistoryService.getDailyActivity(user, date);

            var journalSummary = response.getJournalEntry();
            assertThat(journalSummary.getId()).isEqualTo(1L);
            assertThat(journalSummary.getContent()).isEqualTo("Great progress today");
            assertThat(journalSummary.getMood()).isEqualTo(Mood.GOOD);
            assertThat(journalSummary.getShieldUsed()).isFalse();
        }
    }

    @Nested
    @DisplayName("Has real activity on date tests")
    class HasRealActivityOnDateTests {

        @Test
        @DisplayName("Should return true when task completion exists")
        void shouldReturnTrueWhenTaskCompletionExists() {
            var date = LocalDate.now();
            when(taskCompletionRepository.hasCompletedTaskOnDate(eq(user), any(), eq(date))).thenReturn(true);

            var hasActivity = activityHistoryService.hasRealActivityOnDate(user, date);

            assertThat(hasActivity).isTrue();
        }

        @Test
        @DisplayName("Should return true when progress entry exists")
        void shouldReturnTrueWhenProgressEntryExists() {
            var date = LocalDate.now();
            when(taskCompletionRepository.hasCompletedTaskOnDate(eq(user), any(), eq(date))).thenReturn(false);
            when(progressEntryRepository.hasProgressOnDate(user, date)).thenReturn(true);

            var hasActivity = activityHistoryService.hasRealActivityOnDate(user, date);

            assertThat(hasActivity).isTrue();
        }

        @Test
        @DisplayName("Should return false when no real activity exists")
        void shouldReturnFalseWhenNoRealActivityExists() {
            var date = LocalDate.now();
            when(taskCompletionRepository.hasCompletedTaskOnDate(eq(user), any(), eq(date))).thenReturn(false);
            when(progressEntryRepository.hasProgressOnDate(user, date)).thenReturn(false);

            var hasActivity = activityHistoryService.hasRealActivityOnDate(user, date);

            assertThat(hasActivity).isFalse();
        }
    }

    @Nested
    @DisplayName("Has journal on date tests")
    class HasJournalOnDateTests {

        @Test
        @DisplayName("Should return true when journal exists")
        void shouldReturnTrueWhenJournalExists() {
            var date = LocalDate.now();
            when(dailyJournalRepository.existsByUserAndJournalDate(user, date)).thenReturn(true);

            var hasJournal = activityHistoryService.hasJournalOnDate(user, date);

            assertThat(hasJournal).isTrue();
        }

        @Test
        @DisplayName("Should return false when journal does not exist")
        void shouldReturnFalseWhenJournalDoesNotExist() {
            var date = LocalDate.now();
            when(dailyJournalRepository.existsByUserAndJournalDate(user, date)).thenReturn(false);

            var hasJournal = activityHistoryService.hasJournalOnDate(user, date);

            assertThat(hasJournal).isFalse();
        }
    }
}
