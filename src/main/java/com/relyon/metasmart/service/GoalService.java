package com.relyon.metasmart.service;

import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.constant.LogMessages;
import com.relyon.metasmart.entity.actionplan.ActionItem;
import com.relyon.metasmart.entity.actionplan.dto.ActionItemResponse;
import com.relyon.metasmart.entity.actionplan.dto.TaskCompletionDto;
import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.goal.GoalCategory;
import com.relyon.metasmart.entity.goal.GoalStatus;
import com.relyon.metasmart.entity.goal.dto.*;
import com.relyon.metasmart.entity.guardian.GuardianStatus;
import com.relyon.metasmart.entity.progress.Milestone;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.mapper.ActionItemMapper;
import com.relyon.metasmart.mapper.GoalMapper;
import com.relyon.metasmart.mapper.ScheduledTaskMapper;
import com.relyon.metasmart.repository.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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
public class GoalService {

    private final GoalRepository goalRepository;
    private final MilestoneRepository milestoneRepository;
    private final ProgressEntryRepository progressEntryRepository;
    private final ActionItemRepository actionItemRepository;
    private final ObstacleEntryRepository obstacleEntryRepository;
    private final ScheduledTaskRepository scheduledTaskRepository;
    private final GoalGuardianRepository goalGuardianRepository;
    private final TaskCompletionRepository taskCompletionRepository;
    private final GoalMapper goalMapper;
    private final ActionItemMapper actionItemMapper;
    private final ScheduledTaskMapper scheduledTaskMapper;
    private final UserProfileService userProfileService;
    private final UsageLimitService usageLimitService;

    @Transactional
    public GoalResponse create(GoalRequest request, User owner) {
        log.debug("Creating goal for user ID: {}", owner.getId());

        usageLimitService.enforceGoalLimit(owner);

        var goal = goalMapper.toEntity(request);
        goal.setOwner(owner);

        var savedGoal = goalRepository.save(goal);
        log.info("Goal created with ID: {} for user ID: {}", savedGoal.getId(), owner.getId());

        createDefaultMilestones(savedGoal);

        return enrichGoalResponse(savedGoal);
    }

    private void createDefaultMilestones(Goal goal) {
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
                    log.warn(LogMessages.GOAL_NOT_FOUND_FOR_USER, id, owner.getId());
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
                    log.warn(LogMessages.GOAL_NOT_FOUND_FOR_USER, id, owner.getId());
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
                    log.warn(LogMessages.GOAL_NOT_FOUND_FOR_USER, id, owner.getId());
                    return new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND);
                });

        obstacleEntryRepository.deleteByGoal(goal);
        actionItemRepository.deleteByGoal(goal);
        progressEntryRepository.deleteByGoal(goal);
        milestoneRepository.deleteByGoal(goal);
        goalRepository.delete(goal);
        log.info("Goal deleted with ID: {}", id);
    }

    @Transactional
    public GoalResponse archive(Long id, User owner) {
        log.debug("Archiving goal ID: {} for user ID: {}", id, owner.getId());

        var goal = goalRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> {
                    log.warn(LogMessages.GOAL_NOT_FOUND_FOR_USER, id, owner.getId());
                    return new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND);
                });

        goal.setArchivedAt(LocalDate.now());
        var savedGoal = goalRepository.save(goal);
        log.info("Goal archived with ID: {}", id);
        return enrichGoalResponse(savedGoal);
    }

    @Transactional
    public GoalResponse unarchive(Long id, User owner) {
        log.debug("Unarchiving goal ID: {} for user ID: {}", id, owner.getId());

        var goal = goalRepository.findByIdAndOwnerAndArchivedAtIsNotNull(id, owner)
                .orElseThrow(() -> {
                    log.warn("Archived goal not found with ID: {} for user ID: {}", id, owner.getId());
                    return new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND);
                });

        goal.setArchivedAt(null);
        var savedGoal = goalRepository.save(goal);
        log.info("Goal unarchived with ID: {}", id);
        return enrichGoalResponse(savedGoal);
    }

    @Transactional(readOnly = true)
    public Page<GoalResponse> findArchived(User owner, Pageable pageable) {
        log.debug("Finding archived goals for user ID: {}", owner.getId());
        return goalRepository.findByOwnerAndArchivedAtIsNotNull(owner, pageable)
                .map(this::enrichGoalResponse);
    }

    @Transactional(readOnly = true)
    public Page<GoalResponse> search(User owner, String query, Pageable pageable) {
        log.debug("Searching goals with query: {} for user ID: {}", query, owner.getId());
        return goalRepository.searchByOwner(owner, query, pageable)
                .map(this::enrichGoalResponse);
    }

    @Transactional(readOnly = true)
    public Page<GoalResponse> filter(User owner, GoalStatus status, GoalCategory category, Pageable pageable) {
        log.debug("Filtering goals with status: {}, category: {} for user ID: {}", status, category, owner.getId());
        return goalRepository.findByOwnerWithFilters(owner, status, category, pageable)
                .map(this::enrichGoalResponse);
    }

    @Transactional(readOnly = true)
    public List<GoalResponse> findDueSoon(User owner, int days) {
        log.debug("Finding goals due within {} days for user ID: {}", days, owner.getId());
        var dueDate = LocalDate.now().plusDays(days);
        return goalRepository.findGoalsDueSoon(owner, dueDate).stream()
                .map(this::enrichGoalResponse)
                .toList();
    }

    @Transactional
    public GoalResponse duplicate(Long id, User owner) {
        log.debug("Duplicating goal ID: {} for user ID: {}", id, owner.getId());

        usageLimitService.enforceGoalLimit(owner);

        var original = goalRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> {
                    log.warn(LogMessages.GOAL_NOT_FOUND_FOR_USER, id, owner.getId());
                    return new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND);
                });

        var duplicate = Goal.builder()
                .title(original.getTitle() + " (Copy)")
                .description(original.getDescription())
                .goalCategory(original.getGoalCategory())
                .targetValue(original.getTargetValue())
                .unit(original.getUnit())
                .motivation(original.getMotivation())
                .startDate(LocalDate.now())
                .targetDate(original.getTargetDate() != null
                        ? LocalDate.now().plusDays(java.time.temporal.ChronoUnit.DAYS.between(original.getStartDate(), original.getTargetDate()))
                        : null)
                .owner(owner)
                .build();

        var savedGoal = goalRepository.save(duplicate);
        log.info("Goal duplicated from ID: {} to new ID: {} for user ID: {}", id, savedGoal.getId(), owner.getId());

        createDefaultMilestones(savedGoal);

        return enrichGoalResponse(savedGoal);
    }

    @Transactional
    public GoalResponse useStreakShield(Long goalId, User owner) {
        log.debug("Using streak shield for goal ID: {} by user ID: {}", goalId, owner.getId());

        var goal = goalRepository.findByIdAndOwnerAndArchivedAtIsNull(goalId, owner)
                .orElseThrow(() -> {
                    log.warn(LogMessages.GOAL_NOT_FOUND_FOR_USER, goalId, owner.getId());
                    return new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND);
                });

        // Check if shield was already used today
        if (goal.getLastStreakShieldUsedAt() != null
                && goal.getLastStreakShieldUsedAt().equals(LocalDate.now())) {
            throw new IllegalStateException("Streak shield already used today for this goal");
        }

        // Try to use a shield
        if (!userProfileService.useStreakShield(owner)) {
            throw new IllegalStateException("No streak shields available");
        }

        goal.setLastStreakShieldUsedAt(LocalDate.now());
        var savedGoal = goalRepository.save(goal);
        log.info("Streak shield used for goal ID: {} by user ID: {}", goalId, owner.getId());

        return enrichGoalResponse(savedGoal);
    }

    private GoalResponse enrichGoalResponse(Goal goal) {
        var response = goalMapper.toResponse(goal);
        response.setSmartPillars(calculateSmartPillars(goal));
        response.setSetupCompletionPercentage(calculateSetupCompletion(goal));
        response.setProgressPercentage(calculateProgressPercentage(goal));

        var streaks = calculateStreaks(goal);
        response.setCurrentStreak(streaks[0]);
        response.setLongestStreak(streaks[1]);

        // Populate checkins from progress entries
        response.setCheckins(buildCheckins(goal));

        // Populate action plan with tasks and scheduled tasks
        response.setActionPlan(buildActionPlan(goal));

        // Populate milestones
        response.setMilestones(buildMilestones(goal));

        // Populate support system (guardians as accountability partners)
        response.setSupportSystem(buildSupportSystem(goal));

        return response;
    }

    private List<CheckinDto> buildCheckins(Goal goal) {
        return progressEntryRepository.findByGoalOrderByCreatedAtDesc(goal, Pageable.ofSize(50))
                .getContent()
                .stream()
                .map(entry -> CheckinDto.builder()
                        .id(entry.getId())
                        .createdAt(entry.getCreatedAt())
                        .note(entry.getNote())
                        .progressDelta(entry.getProgressValue())
                        .build())
                .toList();
    }

    private ActionPlanDto buildActionPlan(Goal goal) {
        var actionItems = actionItemRepository.findByGoalOrderByOrderIndexAscCreatedAtAsc(goal);
        var scheduledTasks = scheduledTaskRepository.findByGoalOrderByScheduledDateAsc(goal);

        var taskResponses = actionItems.stream()
                .map(this::enrichActionItemWithCompletionHistory)
                .toList();

        var scheduledTaskDtos = scheduledTasks.stream()
                .map(scheduledTaskMapper::toDto)
                .toList();

        return ActionPlanDto.builder()
                .overview(goal.getActionPlanOverview())
                .tasks(taskResponses)
                .scheduledTasks(scheduledTaskDtos)
                .build();
    }

    private ActionItemResponse enrichActionItemWithCompletionHistory(ActionItem actionItem) {
        var response = actionItemMapper.toResponse(actionItem);
        var completions = taskCompletionRepository.findByActionItemOrderByCompletedAtDesc(actionItem);
        var completionDtos = completions.stream()
                .map(completion -> TaskCompletionDto.builder()
                        .id(completion.getId())
                        .date(completion.getDate())
                        .completedAt(completion.getCompletedAt())
                        .note(completion.getNote())
                        .build())
                .toList();
        response.setCompletionHistory(completionDtos);
        return response;
    }

    private List<MilestoneDto> buildMilestones(Goal goal) {
        return milestoneRepository.findByGoalOrderByPercentageAsc(goal)
                .stream()
                .map(milestone -> MilestoneDto.builder()
                        .value(BigDecimal.valueOf(milestone.getPercentage()))
                        .label(milestone.getDescription())
                        .achieved(milestone.getAchieved())
                        .build())
                .toList();
    }

    private SupportSystemDto buildSupportSystem(Goal goal) {
        var guardians = goalGuardianRepository.findByGoalAndStatus(goal, GuardianStatus.ACTIVE);
        var partners = guardians.stream()
                .map(guardian -> SupportSystemDto.AccountabilityPartnerDto.builder()
                        .name(guardian.getGuardian().getName())
                        .contact(guardian.getGuardian().getEmail())
                        .relation("Guardian")
                        .build())
                .toList();
        return SupportSystemDto.builder()
                .accountabilityPartners(partners)
                .build();
    }

    private SmartPillarsDto calculateSmartPillars(Goal goal) {
        var specific = hasContent(goal.getTitle()) && hasContent(goal.getDescription());
        var measurable = goal.getTargetValue() != null && hasContent(goal.getUnit());
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
        if (goal.getTargetValue() != null) completedFields++;
        if (hasContent(goal.getUnit())) completedFields++;
        if (hasContent(goal.getMotivation())) completedFields++;
        if (goal.getStartDate() != null) completedFields++;
        if (goal.getTargetDate() != null) completedFields++;

        return (completedFields * 100) / totalFields;
    }

    private BigDecimal calculateProgressPercentage(Goal goal) {
        if (goal.getTargetValue() == null || goal.getTargetValue().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return goal.getCurrentProgress()
                .multiply(BigDecimal.valueOf(100))
                .divide(goal.getTargetValue(), 2, RoundingMode.HALF_UP);
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

        // Check if streak shield was used within recovery window (today or yesterday)
        var shieldUsed = goal.getLastStreakShieldUsedAt() != null
                && (goal.getLastStreakShieldUsedAt().equals(today)
                || goal.getLastStreakShieldUsedAt().equals(today.minusDays(1)));

        if (dates.getFirst().equals(today) || dates.getFirst().equals(today.minusDays(1)) || shieldUsed) {
            currentStreak = 1;
        }

        for (var dateIndex = 0; dateIndex < dates.size() - 1; dateIndex++) {
            var current = dates.get(dateIndex);
            var next = dates.get(dateIndex + 1);

            // Check if this gap was covered by a shield
            var gapCoveredByShield = goal.getLastStreakShieldUsedAt() != null
                    && goal.getLastStreakShieldUsedAt().equals(current.minusDays(1))
                    && next.equals(current.minusDays(2));

            if (current.minusDays(1).equals(next) || gapCoveredByShield) {
                streak++;
                if (dateIndex == 0 || dates.getFirst().equals(today) || dates.getFirst().equals(today.minusDays(1)) || shieldUsed) {
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
