package com.relyon.metasmart.service;

import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.goal.GoalCategory;
import com.relyon.metasmart.entity.goal.GoalStatus;
import com.relyon.metasmart.entity.goal.dto.GoalRequest;
import com.relyon.metasmart.entity.goal.dto.GoalResponse;
import com.relyon.metasmart.entity.goal.dto.SmartPillarsDto;
import com.relyon.metasmart.entity.goal.dto.UpdateGoalRequest;
import com.relyon.metasmart.entity.progress.Milestone;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.mapper.GoalMapper;
import com.relyon.metasmart.repository.ActionItemRepository;
import com.relyon.metasmart.repository.GoalRepository;
import com.relyon.metasmart.repository.MilestoneRepository;
import com.relyon.metasmart.repository.ObstacleEntryRepository;
import com.relyon.metasmart.repository.ProgressEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final MilestoneRepository milestoneRepository;
    private final ProgressEntryRepository progressEntryRepository;
    private final ActionItemRepository actionItemRepository;
    private final ObstacleEntryRepository obstacleEntryRepository;
    private final GoalMapper goalMapper;

    @Transactional
    public GoalResponse create(GoalRequest request, User owner) {
        log.debug("Creating goal for user ID: {}", owner.getId());

        var goal = goalMapper.toEntity(request);
        goal.setOwner(owner);

        var savedGoal = goalRepository.save(goal);
        log.info("Goal created with ID: {} for user ID: {}", savedGoal.getId(), owner.getId());

        createDefaultMilestones(savedGoal);

        return enrichGoalResponse(savedGoal);
    }

    private void createDefaultMilestones(com.relyon.metasmart.entity.goal.Goal goal) {
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
        log.debug("Default milestones created for goal ID: {}", goal.getId());
    }

    @Transactional(readOnly = true)
    public GoalResponse findById(Long id, User owner) {
        log.debug("Finding goal ID: {} for user ID: {}", id, owner.getId());
        return goalRepository.findByIdAndOwner(id, owner)
                .map(this::enrichGoalResponse)
                .orElseThrow(() -> {
                    log.warn("Goal not found with ID: {} for user ID: {}", id, owner.getId());
                    return new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND);
                });
    }

    @Transactional(readOnly = true)
    public Page<GoalResponse> findAll(User owner, Pageable pageable) {
        log.debug("Finding all goals for user ID: {}", owner.getId());
        return goalRepository.findByOwner(owner, pageable)
                .map(this::enrichGoalResponse);
    }

    @Transactional(readOnly = true)
    public Page<GoalResponse> findByStatus(User owner, GoalStatus goalStatus, Pageable pageable) {
        log.debug("Finding goals by status: {} for user ID: {}", goalStatus, owner.getId());
        return goalRepository.findByOwnerAndGoalStatus(owner, goalStatus, pageable)
                .map(this::enrichGoalResponse);
    }

    @Transactional(readOnly = true)
    public Page<GoalResponse> findByCategory(User owner, GoalCategory goalCategory, Pageable pageable) {
        log.debug("Finding goals by category: {} for user ID: {}", goalCategory, owner.getId());
        return goalRepository.findByOwnerAndGoalCategory(owner, goalCategory, pageable)
                .map(this::enrichGoalResponse);
    }

    @Transactional
    public GoalResponse update(Long id, UpdateGoalRequest request, User owner) {
        log.debug("Updating goal ID: {} for user ID: {}", id, owner.getId());

        var goal = goalRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> {
                    log.warn("Goal not found with ID: {} for user ID: {}", id, owner.getId());
                    return new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND);
                });

        Optional.ofNullable(request.getTitle()).ifPresent(goal::setTitle);
        Optional.ofNullable(request.getDescription()).ifPresent(goal::setDescription);
        Optional.ofNullable(request.getGoalCategory()).ifPresent(goal::setGoalCategory);
        Optional.ofNullable(request.getTargetValue()).ifPresent(goal::setTargetValue);
        Optional.ofNullable(request.getUnit()).ifPresent(goal::setUnit);
        Optional.ofNullable(request.getCurrentProgress()).ifPresent(goal::setCurrentProgress);
        Optional.ofNullable(request.getMotivation()).ifPresent(goal::setMotivation);
        Optional.ofNullable(request.getStartDate()).ifPresent(goal::setStartDate);
        Optional.ofNullable(request.getTargetDate()).ifPresent(goal::setTargetDate);
        Optional.ofNullable(request.getGoalStatus()).ifPresent(goal::setGoalStatus);

        var savedGoal = goalRepository.save(goal);
        log.info("Goal updated with ID: {}", savedGoal.getId());
        return enrichGoalResponse(savedGoal);
    }

    @Transactional
    public void delete(Long id, User owner) {
        log.debug("Deleting goal ID: {} for user ID: {}", id, owner.getId());

        var goal = goalRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> {
                    log.warn("Goal not found with ID: {} for user ID: {}", id, owner.getId());
                    return new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND);
                });

        obstacleEntryRepository.deleteByGoal(goal);
        actionItemRepository.deleteByGoal(goal);
        progressEntryRepository.deleteByGoal(goal);
        milestoneRepository.deleteByGoal(goal);
        goalRepository.delete(goal);
        log.info("Goal deleted with ID: {}", id);
    }

    private GoalResponse enrichGoalResponse(Goal goal) {
        var response = goalMapper.toResponse(goal);
        response.setSmartPillars(calculateSmartPillars(goal));
        response.setSetupCompletionPercentage(calculateSetupCompletion(goal));
        response.setProgressPercentage(calculateProgressPercentage(goal));

        var streaks = calculateStreaks(goal);
        response.setCurrentStreak(streaks[0]);
        response.setLongestStreak(streaks[1]);

        return response;
    }

    private SmartPillarsDto calculateSmartPillars(Goal goal) {
        var specific = hasContent(goal.getTitle()) && hasContent(goal.getDescription());
        var measurable = hasContent(goal.getTargetValue()) && hasContent(goal.getUnit());
        var achievable = hasContent(goal.getMotivation());
        var relevant = goal.getGoalCategory() != null && hasContent(goal.getMotivation());
        var timeBound = goal.getStartDate() != null && goal.getTargetDate() != null;

        var completedCount = 0;
        if (specific) completedCount++;
        if (measurable) completedCount++;
        if (achievable) completedCount++;
        if (relevant) completedCount++;
        if (timeBound) completedCount++;

        var completionPercentage = (completedCount * 100) / 5;

        return SmartPillarsDto.builder()
                .specific(specific)
                .measurable(measurable)
                .achievable(achievable)
                .relevant(relevant)
                .timeBound(timeBound)
                .completionPercentage(completionPercentage)
                .build();
    }

    private Integer calculateSetupCompletion(Goal goal) {
        var totalFields = 8;
        var completedFields = 0;

        if (hasContent(goal.getTitle())) completedFields++;
        if (hasContent(goal.getDescription())) completedFields++;
        if (goal.getGoalCategory() != null) completedFields++;
        if (hasContent(goal.getTargetValue())) completedFields++;
        if (hasContent(goal.getUnit())) completedFields++;
        if (hasContent(goal.getMotivation())) completedFields++;
        if (goal.getStartDate() != null) completedFields++;
        if (goal.getTargetDate() != null) completedFields++;

        return (completedFields * 100) / totalFields;
    }

    private BigDecimal calculateProgressPercentage(Goal goal) {
        try {
            var targetValue = new BigDecimal(goal.getTargetValue());
            if (targetValue.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }
            return goal.getCurrentProgress()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(targetValue, 2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            log.warn("Invalid target value for goal ID: {}", goal.getId());
            return BigDecimal.ZERO;
        }
    }

    private int[] calculateStreaks(Goal goal) {
        var dates = progressEntryRepository.findDistinctProgressDates(goal);
        if (dates.isEmpty()) {
            return new int[]{0, 0};
        }

        var currentStreak = 0;
        var longestStreak = 0;
        var streak = 1;
        var today = LocalDate.now();

        if (dates.get(0).equals(today) || dates.get(0).equals(today.minusDays(1))) {
            currentStreak = 1;
        }

        for (var i = 0; i < dates.size() - 1; i++) {
            var current = dates.get(i);
            var next = dates.get(i + 1);

            if (current.minusDays(1).equals(next)) {
                streak++;
                if (i == 0 || (i > 0 && dates.get(0).equals(today) || dates.get(0).equals(today.minusDays(1)))) {
                    currentStreak = streak;
                }
            } else {
                longestStreak = Math.max(longestStreak, streak);
                streak = 1;
            }
        }
        longestStreak = Math.max(longestStreak, streak);

        return new int[]{currentStreak, longestStreak};
    }

    private boolean hasContent(String value) {
        return value != null && !value.isBlank();
    }
}
