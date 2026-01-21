package com.relyon.metasmart.service;

import com.relyon.metasmart.entity.actionplan.CompletionStatus;
import com.relyon.metasmart.entity.actionplan.TaskCompletion;
import com.relyon.metasmart.entity.history.dto.*;
import com.relyon.metasmart.entity.journal.DailyJournal;
import com.relyon.metasmart.entity.progress.ProgressEntry;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.repository.DailyJournalRepository;
import com.relyon.metasmart.repository.ProgressEntryRepository;
import com.relyon.metasmart.repository.TaskCompletionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityHistoryService {

    private final TaskCompletionRepository taskCompletionRepository;
    private final ProgressEntryRepository progressEntryRepository;
    private final DailyJournalRepository dailyJournalRepository;

    @Transactional(readOnly = true)
    public ActivityHistoryResponse getActivityHistory(User user, LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching activity history for user: {} from {} to {}", user.getEmail(), startDate, endDate);

        var taskCompletions = taskCompletionRepository.findByUserAndDateRange(user, startDate, endDate);
        var progressEntries = progressEntryRepository.findByUserAndDateRange(
                user,
                startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX));
        var journalEntries = dailyJournalRepository.findByUserAndJournalDateBetweenOrderByJournalDateDesc(
                user, startDate, endDate);

        var tasksByDate = groupTasksByDate(taskCompletions);
        var progressByDate = groupProgressByDate(progressEntries);
        var journalsByDate = journalEntries.stream()
                .collect(Collectors.toMap(DailyJournal::getJournalDate, j -> j));

        var dailyActivities = new LinkedHashMap<LocalDate, DailyActivityResponse>();
        var activeDays = 0;

        for (var date = endDate; !date.isBefore(startDate); date = date.minusDays(1)) {
            var tasks = tasksByDate.getOrDefault(date, List.of());
            var progress = progressByDate.getOrDefault(date, List.of());
            var journal = journalsByDate.get(date);

            var hasActivity = !tasks.isEmpty() || !progress.isEmpty() || journal != null;
            if (hasActivity) {
                activeDays++;
            }

            var dailyActivity = DailyActivityResponse.builder()
                    .date(date)
                    .taskCompletions(tasks.stream().map(this::toTaskSummary).toList())
                    .progressEntries(progress.stream().map(this::toProgressSummary).toList())
                    .journalEntry(journal != null ? toJournalSummary(journal) : null)
                    .hasActivity(hasActivity)
                    .build();

            dailyActivities.put(date, dailyActivity);
        }

        var summary = ActivityHistoryResponse.ActivitySummary.builder()
                .totalTaskCompletions(taskCompletions.size())
                .totalProgressEntries(progressEntries.size())
                .totalJournalEntries(journalEntries.size())
                .build();

        return ActivityHistoryResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalDays((int) (endDate.toEpochDay() - startDate.toEpochDay() + 1))
                .activeDays(activeDays)
                .dailyActivities(dailyActivities)
                .summary(summary)
                .build();
    }

    @Transactional(readOnly = true)
    public DailyActivityResponse getDailyActivity(User user, LocalDate date) {
        log.debug("Fetching daily activity for user: {} on date: {}", user.getEmail(), date);

        var taskCompletions = taskCompletionRepository.findByUserAndDateRange(user, date, date);
        var progressEntries = progressEntryRepository.findByUserAndDateRange(
                user,
                date.atStartOfDay(),
                date.atTime(LocalTime.MAX));
        var journalEntry = dailyJournalRepository.findByUserAndJournalDate(user, date).orElse(null);

        var hasActivity = !taskCompletions.isEmpty() || !progressEntries.isEmpty() || journalEntry != null;

        return DailyActivityResponse.builder()
                .date(date)
                .taskCompletions(taskCompletions.stream().map(this::toTaskSummary).toList())
                .progressEntries(progressEntries.stream().map(this::toProgressSummary).toList())
                .journalEntry(journalEntry != null ? toJournalSummary(journalEntry) : null)
                .hasActivity(hasActivity)
                .build();
    }

    public boolean hasRealActivityOnDate(User user, LocalDate date) {
        var hasTaskCompletion = taskCompletionRepository.hasCompletedTaskOnDate(
                user,
                List.of(CompletionStatus.COMPLETED, CompletionStatus.PARTIAL),
                date);

        if (hasTaskCompletion) {
            return true;
        }

        return progressEntryRepository.hasProgressOnDate(user, date);
    }

    public boolean hasJournalOnDate(User user, LocalDate date) {
        return dailyJournalRepository.existsByUserAndJournalDate(user, date);
    }

    private Map<LocalDate, List<TaskCompletion>> groupTasksByDate(List<TaskCompletion> completions) {
        return completions.stream()
                .collect(Collectors.groupingBy(TaskCompletion::getScheduledDate));
    }

    private Map<LocalDate, List<ProgressEntry>> groupProgressByDate(List<ProgressEntry> entries) {
        return entries.stream()
                .collect(Collectors.groupingBy(e -> e.getCreatedAt().toLocalDate()));
    }

    private TaskCompletionSummary toTaskSummary(TaskCompletion tc) {
        var actionItem = tc.getActionItem();
        var goal = actionItem.getGoal();

        return TaskCompletionSummary.builder()
                .id(tc.getId())
                .actionItemId(actionItem.getId())
                .actionItemTitle(actionItem.getTitle())
                .goalId(goal.getId())
                .goalTitle(goal.getTitle())
                .status(tc.getStatus())
                .note(tc.getNote())
                .completedAt(tc.getCompletedAt())
                .build();
    }

    private ProgressEntrySummary toProgressSummary(ProgressEntry pe) {
        var goal = pe.getGoal();
        var percentage = BigDecimal.ZERO;

        if (goal.getTargetValue() != null && goal.getTargetValue().compareTo(BigDecimal.ZERO) > 0) {
            percentage = pe.getProgressValue()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(goal.getTargetValue(), 2, RoundingMode.HALF_UP);
        }

        return ProgressEntrySummary.builder()
                .id(pe.getId())
                .goalId(goal.getId())
                .goalTitle(goal.getTitle())
                .progressValue(pe.getProgressValue())
                .unit(goal.getUnit())
                .percentageOfGoal(percentage)
                .note(pe.getNote())
                .createdAt(pe.getCreatedAt())
                .build();
    }

    private JournalEntrySummary toJournalSummary(DailyJournal journal) {
        return JournalEntrySummary.builder()
                .id(journal.getId())
                .content(journal.getContent())
                .mood(journal.getMood())
                .shieldUsed(journal.getShieldUsed())
                .createdAt(journal.getCreatedAt())
                .build();
    }
}
