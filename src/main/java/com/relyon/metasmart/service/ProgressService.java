package com.relyon.metasmart.service;

import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.goal.GoalStatus;
import com.relyon.metasmart.entity.progress.Milestone;
import com.relyon.metasmart.entity.progress.dto.MilestoneRequest;
import com.relyon.metasmart.entity.progress.dto.MilestoneResponse;
import com.relyon.metasmart.entity.progress.dto.ProgressEntryRequest;
import com.relyon.metasmart.entity.progress.dto.ProgressEntryResponse;
import com.relyon.metasmart.entity.progress.dto.UpdateProgressEntryRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.DuplicateResourceException;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.mapper.MilestoneMapper;
import com.relyon.metasmart.mapper.ProgressEntryMapper;
import com.relyon.metasmart.repository.GoalRepository;
import com.relyon.metasmart.repository.MilestoneRepository;
import com.relyon.metasmart.repository.ProgressEntryRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgressService {

    private final ProgressEntryRepository progressEntryRepository;
    private final MilestoneRepository milestoneRepository;
    private final GoalRepository goalRepository;
    private final ProgressEntryMapper progressEntryMapper;
    private final MilestoneMapper milestoneMapper;
    private final UserProfileService userProfileService;

    @Transactional
    public ProgressEntryResponse addProgress(Long goalId, ProgressEntryRequest request, User user) {
        log.debug("Adding progress entry for goal ID: {}", goalId);

        var goal = findGoalByIdAndOwner(goalId, user);
        var entry = progressEntryMapper.toEntity(request);
        entry.setGoal(goal);

        var savedEntry = progressEntryRepository.save(entry);
        log.info("Progress entry created with ID: {} for goal ID: {}", savedEntry.getId(), goalId);

        updateGoalProgress(goal);
        checkAndUpdateMilestones(goal, user);

        return progressEntryMapper.toResponse(savedEntry);
    }

    @Transactional(readOnly = true)
    public Page<ProgressEntryResponse> getProgressHistory(Long goalId, User user, Pageable pageable) {
        log.debug("Fetching progress history for goal ID: {}", goalId);

        var goal = findGoalByIdAndOwner(goalId, user);
        return progressEntryRepository.findByGoalOrderByCreatedAtDesc(goal, pageable)
                .map(progressEntryMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProgressEntryResponse> getProgressHistoryByDateRange(
            Long goalId, User user, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.debug("Fetching progress history for goal ID: {} between {} and {}", goalId, startDate, endDate);

        var goal = findGoalByIdAndOwner(goalId, user);
        var startDateTime = startDate.atStartOfDay();
        var endDateTime = endDate.atTime(LocalTime.MAX);

        return progressEntryRepository.findByGoalAndCreatedAtBetweenOrderByCreatedAtDesc(
                        goal, startDateTime, endDateTime, pageable)
                .map(progressEntryMapper::toResponse);
    }

    @Transactional
    public ProgressEntryResponse updateProgressEntry(Long goalId, Long entryId, UpdateProgressEntryRequest request, User user) {
        log.debug("Updating progress entry ID: {} for goal ID: {}", entryId, goalId);

        var goal = findGoalByIdAndOwner(goalId, user);
        var entry = progressEntryRepository.findByIdAndGoal(entryId, goal)
                .orElseThrow(() -> {
                    log.warn("Progress entry not found with ID: {} for goal ID: {}", entryId, goalId);
                    return new ResourceNotFoundException(ErrorMessages.PROGRESS_ENTRY_NOT_FOUND);
                });

        Optional.ofNullable(request.getProgressValue()).ifPresent(entry::setProgressValue);
        Optional.ofNullable(request.getNote()).ifPresent(entry::setNote);

        var savedEntry = progressEntryRepository.save(entry);
        log.info("Progress entry updated with ID: {} for goal ID: {}", savedEntry.getId(), goalId);

        updateGoalProgress(goal);
        recheckMilestones(goal);

        return progressEntryMapper.toResponse(savedEntry);
    }

    @Transactional
    public void deleteProgressEntry(Long goalId, Long entryId, User user) {
        log.debug("Deleting progress entry ID: {} from goal ID: {}", entryId, goalId);

        var goal = findGoalByIdAndOwner(goalId, user);
        var entry = progressEntryRepository.findById(entryId)
                .filter(e -> e.getGoal().getId().equals(goalId))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.PROGRESS_ENTRY_NOT_FOUND));

        progressEntryRepository.delete(entry);
        log.info("Progress entry ID: {} deleted from goal ID: {}", entryId, goalId);

        updateGoalProgress(goal);
        recheckMilestones(goal);
    }

    @Transactional
    public MilestoneResponse addMilestone(Long goalId, MilestoneRequest request, User user) {
        log.debug("Adding milestone for goal ID: {} at {}%", goalId, request.getPercentage());

        var goal = findGoalByIdAndOwner(goalId, user);

        if (milestoneRepository.existsByGoalAndPercentage(goal, request.getPercentage())) {
            log.warn("Milestone at {}% already exists for goal ID: {}", request.getPercentage(), goalId);
            throw new DuplicateResourceException(ErrorMessages.MILESTONE_ALREADY_EXISTS);
        }

        var milestone = milestoneMapper.toEntity(request);
        milestone.setGoal(goal);

        var currentPercentage = calculateProgressPercentage(goal);
        if (currentPercentage.compareTo(BigDecimal.valueOf(request.getPercentage())) >= 0) {
            milestone.setAchieved(true);
            milestone.setAchievedAt(LocalDateTime.now());
            log.info("Milestone at {}% immediately achieved for goal ID: {}", request.getPercentage(), goalId);
        }

        var savedMilestone = milestoneRepository.save(milestone);
        log.info("Milestone created with ID: {} for goal ID: {}", savedMilestone.getId(), goalId);

        return milestoneMapper.toResponse(savedMilestone);
    }

    @Transactional(readOnly = true)
    public List<MilestoneResponse> getMilestones(Long goalId, User user) {
        log.debug("Fetching milestones for goal ID: {}", goalId);

        var goal = findGoalByIdAndOwner(goalId, user);
        return milestoneRepository.findByGoalOrderByPercentageAsc(goal).stream()
                .map(milestoneMapper::toResponse)
                .toList();
    }

    @Transactional
    public void deleteMilestone(Long goalId, Long milestoneId, User user) {
        log.debug("Deleting milestone ID: {} from goal ID: {}", milestoneId, goalId);

        var goal = findGoalByIdAndOwner(goalId, user);
        var milestone = milestoneRepository.findByIdAndGoal(milestoneId, goal)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.MILESTONE_NOT_FOUND));

        milestoneRepository.delete(milestone);
        log.info("Milestone ID: {} deleted from goal ID: {}", milestoneId, goalId);
    }

    @Transactional
    public void createDefaultMilestones(Goal goal) {
        log.debug("Creating default milestones for goal ID: {}", goal.getId());

        var percentages = List.of(25, 50, 75, 100);
        for (var percentage : percentages) {
            var milestone = Milestone.builder()
                    .goal(goal)
                    .percentage(percentage)
                    .description(percentage + "% completed")
                    .achieved(false)
                    .build();
            milestoneRepository.save(milestone);
        }
        log.info("Default milestones created for goal ID: {}", goal.getId());
    }

    private Goal findGoalByIdAndOwner(Long goalId, User user) {
        return goalRepository.findByIdAndOwner(goalId, user)
                .orElseThrow(() -> {
                    log.warn("Goal not found with ID: {} for user ID: {}", goalId, user.getId());
                    return new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND);
                });
    }

    private void updateGoalProgress(Goal goal) {
        var totalProgress = progressEntryRepository.sumValueByGoal(goal);
        goal.setCurrentProgress(totalProgress);
        goalRepository.save(goal);
        log.debug("Goal ID: {} progress updated to: {}", goal.getId(), totalProgress);

        var targetValue = new BigDecimal(goal.getTargetValue());
        if (totalProgress.compareTo(targetValue) >= 0 && goal.getGoalStatus() == GoalStatus.ACTIVE) {
            goal.setGoalStatus(GoalStatus.COMPLETED);
            goalRepository.save(goal);
            log.info("Goal ID: {} marked as COMPLETED", goal.getId());
        }
    }

    private void checkAndUpdateMilestones(Goal goal, User user) {
        var currentPercentage = calculateProgressPercentage(goal);
        var unachievedMilestones = milestoneRepository.findByGoalAndAchievedFalseOrderByPercentageAsc(goal);

        for (var milestone : unachievedMilestones) {
            if (currentPercentage.compareTo(BigDecimal.valueOf(milestone.getPercentage())) >= 0) {
                milestone.setAchieved(true);
                milestone.setAchievedAt(LocalDateTime.now());
                milestoneRepository.save(milestone);
                log.info("Milestone {}% achieved for goal ID: {}", milestone.getPercentage(), goal.getId());

                // Award streak shield for major milestones (50% and 100%)
                if (milestone.getPercentage() == 50 || milestone.getPercentage() == 100) {
                    userProfileService.addStreakShield(user, 1);
                    log.info("Streak shield awarded to user {} for reaching {}% milestone", user.getEmail(), milestone.getPercentage());
                }
            }
        }
    }

    private void recheckMilestones(Goal goal) {
        var currentPercentage = calculateProgressPercentage(goal);
        var milestones = milestoneRepository.findByGoalOrderByPercentageAsc(goal);

        for (var milestone : milestones) {
            var shouldBeAchieved = currentPercentage.compareTo(BigDecimal.valueOf(milestone.getPercentage())) >= 0;
            if (Boolean.TRUE.equals(milestone.getAchieved()) && !shouldBeAchieved) {
                milestone.setAchieved(false);
                milestone.setAchievedAt(null);
                milestoneRepository.save(milestone);
                log.info("Milestone {}% reverted for goal ID: {}", milestone.getPercentage(), goal.getId());
            }
        }
    }

    private BigDecimal calculateProgressPercentage(Goal goal) {
        var targetValue = new BigDecimal(goal.getTargetValue());
        if (targetValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return goal.getCurrentProgress()
                .multiply(BigDecimal.valueOf(100))
                .divide(targetValue, 2, RoundingMode.HALF_UP);
    }
}
