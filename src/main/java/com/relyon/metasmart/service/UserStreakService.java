package com.relyon.metasmart.service;

import static com.relyon.metasmart.constant.AppConstants.MAX_STREAK_SHIELDS;
import static com.relyon.metasmart.constant.AppConstants.SHIELDS_PER_WEEK_FROM_JOURNAL;

import com.relyon.metasmart.entity.actionplan.CompletionStatus;
import com.relyon.metasmart.entity.streak.StreakInfo;
import com.relyon.metasmart.entity.streak.dto.UserStreakResponse;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.repository.DailyJournalRepository;
import com.relyon.metasmart.repository.ProgressEntryRepository;
import com.relyon.metasmart.repository.StreakInfoRepository;
import com.relyon.metasmart.repository.TaskCompletionRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserStreakService {

    private final StreakInfoRepository streakInfoRepository;
    private final TaskCompletionRepository taskCompletionRepository;
    private final ProgressEntryRepository progressEntryRepository;
    private final DailyJournalRepository dailyJournalRepository;
    private final UserProfileService userProfileService;

    @Transactional(readOnly = true)
    public UserStreakResponse getUserStreak(User user) {
        log.debug("Getting user streak for: {}", user.getEmail());

        var streakInfo = streakInfoRepository.findByUserAndGoalIsNullAndActionItemIsNull(user)
                .orElse(createDefaultStreakInfo(user));

        var today = LocalDate.now();
        var weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        var weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        var shieldsUsedThisWeek = dailyJournalRepository.countShieldsUsedInWeek(user, weekStart, weekEnd);
        var shieldsAvailable = Math.min(
                user.getStreakShields(),
                MAX_STREAK_SHIELDS - shieldsUsedThisWeek);

        var journalCountThisMonth = dailyJournalRepository.countByUserAndMonth(
                user,
                today.withDayOfMonth(1),
                today.withDayOfMonth(today.lengthOfMonth()));

        return UserStreakResponse.builder()
                .currentStreak(streakInfo.getCurrentMaintainedStreak())
                .bestStreak(streakInfo.getBestMaintainedStreak())
                .shieldsAvailable(shieldsAvailable)
                .shieldsUsedThisWeek(shieldsUsedThisWeek)
                .journalEntriesThisMonth(journalCountThisMonth)
                .lastActivityDate(streakInfo.getLastUpdatedAt() != null
                        ? streakInfo.getLastUpdatedAt().toLocalDate()
                        : null)
                .build();
    }

    @Transactional
    public void processEndOfDay(User user, LocalDate date) {
        log.debug("Processing end of day streak for user: {} on date: {}", user.getEmail(), date);

        var hasRealActivity = hasRealActivityOnDate(user, date);
        var hasJournal = dailyJournalRepository.existsByUserAndJournalDate(user, date);

        var streakInfo = streakInfoRepository.findByUserAndGoalIsNullAndActionItemIsNull(user)
                .orElseGet(() -> createAndSaveDefaultStreakInfo(user));

        if (hasRealActivity) {
            streakInfo.incrementMaintainedStreak();
            log.info("Streak incremented for user: {} - now at {}", user.getEmail(), streakInfo.getCurrentMaintainedStreak());
        } else if (hasJournal && canUseShield(user, date)) {
            useShield(user, date, streakInfo);
            log.info("Shield used to protect streak for user: {}", user.getEmail());
        } else {
            if (streakInfo.getCurrentMaintainedStreak() > 0) {
                streakInfo.resetMaintainedStreak();
                log.info("Streak reset for user: {}", user.getEmail());
            }
        }

        streakInfoRepository.save(streakInfo);
    }

    @Transactional
    public void recalculateStreak(User user) {
        log.debug("Recalculating streak for user: {}", user.getEmail());

        var today = LocalDate.now();
        var streakInfo = streakInfoRepository.findByUserAndGoalIsNullAndActionItemIsNull(user)
                .orElseGet(() -> createAndSaveDefaultStreakInfo(user));

        var currentStreak = 0;
        var date = today;

        while (true) {
            var hasActivity = hasRealActivityOnDate(user, date);
            var hasJournal = dailyJournalRepository.existsByUserAndJournalDate(user, date);
            var shieldWasUsed = dailyJournalRepository.findByUserAndJournalDate(user, date)
                    .map(j -> Boolean.TRUE.equals(j.getShieldUsed()))
                    .orElse(false);

            if (hasActivity || shieldWasUsed) {
                currentStreak++;
                date = date.minusDays(1);
            } else if (hasJournal && date.equals(today)) {
                currentStreak++;
                date = date.minusDays(1);
            } else {
                break;
            }

            if (date.isBefore(today.minusYears(1))) {
                break;
            }
        }

        streakInfo.setCurrentMaintainedStreak(currentStreak);
        if (currentStreak > streakInfo.getBestMaintainedStreak()) {
            streakInfo.setBestMaintainedStreak(currentStreak);
        }
        streakInfo.setLastUpdatedAt(LocalDateTime.now());

        streakInfoRepository.save(streakInfo);
        log.info("Streak recalculated for user: {} - current: {}, best: {}",
                user.getEmail(), currentStreak, streakInfo.getBestMaintainedStreak());
    }

    @Transactional
    public void onActivityRecorded(User user) {
        log.debug("Activity recorded for user: {}, updating streak", user.getEmail());

        var today = LocalDate.now();
        var streakInfo = streakInfoRepository.findByUserAndGoalIsNullAndActionItemIsNull(user)
                .orElseGet(() -> createAndSaveDefaultStreakInfo(user));

        var lastActivityDate = streakInfo.getLastUpdatedAt() != null
                ? streakInfo.getLastUpdatedAt().toLocalDate()
                : null;

        if (lastActivityDate == null || lastActivityDate.isBefore(today.minusDays(1))) {
            recalculateStreak(user);
        } else if (lastActivityDate.isBefore(today)) {
            streakInfo.incrementMaintainedStreak();
            streakInfoRepository.save(streakInfo);
            log.info("Streak incremented to {} for user: {}", streakInfo.getCurrentMaintainedStreak(), user.getEmail());
        }
    }

    @Transactional
    public void awardJournalShield(User user, LocalDate journalDate) {
        var weekStart = journalDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        var weekEnd = journalDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        var shieldsAwardedThisWeek = dailyJournalRepository.countShieldsUsedInWeek(user, weekStart, weekEnd);

        if (shieldsAwardedThisWeek < SHIELDS_PER_WEEK_FROM_JOURNAL
                && user.getStreakShields() < MAX_STREAK_SHIELDS) {
            userProfileService.addStreakShield(user, 1);
            log.info("Journal shield awarded to user: {}", user.getEmail());
        }
    }

    private boolean hasRealActivityOnDate(User user, LocalDate date) {
        var hasTaskCompletion = taskCompletionRepository.hasCompletedTaskOnDate(
                user,
                List.of(CompletionStatus.COMPLETED, CompletionStatus.PARTIAL),
                date);

        if (hasTaskCompletion) {
            return true;
        }

        return progressEntryRepository.hasProgressOnDate(user, date);
    }

    private boolean canUseShield(User user, LocalDate date) {
        if (user.getStreakShields() <= 0) {
            return false;
        }

        var weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        var weekEnd = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        var shieldsUsedThisWeek = dailyJournalRepository.countShieldsUsedInWeek(user, weekStart, weekEnd);

        return shieldsUsedThisWeek < SHIELDS_PER_WEEK_FROM_JOURNAL;
    }

    private void useShield(User user, LocalDate date, StreakInfo streakInfo) {
        userProfileService.useStreakShield(user, 1);

        dailyJournalRepository.findByUserAndJournalDate(user, date)
                .ifPresent(journal -> {
                    journal.setShieldUsed(true);
                    dailyJournalRepository.save(journal);
                });

        streakInfo.setLastUpdatedAt(LocalDateTime.now());
    }

    private StreakInfo createDefaultStreakInfo(User user) {
        return StreakInfo.builder()
                .user(user)
                .currentMaintainedStreak(0)
                .bestMaintainedStreak(0)
                .currentPerfectStreak(0)
                .bestPerfectStreak(0)
                .build();
    }

    private StreakInfo createAndSaveDefaultStreakInfo(User user) {
        var streakInfo = createDefaultStreakInfo(user);
        return streakInfoRepository.save(streakInfo);
    }
}
