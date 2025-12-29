package com.relyon.metasmart.service;

import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.goal.GoalStatus;
import com.relyon.metasmart.entity.reflection.GoalReflection;
import com.relyon.metasmart.entity.reflection.ReflectionFrequency;
import com.relyon.metasmart.entity.reflection.dto.*;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.BadRequestException;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.repository.GoalReflectionRepository;
import com.relyon.metasmart.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReflectionService {

    private final GoalReflectionRepository reflectionRepository;
    private final GoalRepository goalRepository;

    public ReflectionStatusResponse getReflectionStatus(Long goalId, User user) {
        log.debug("Getting reflection status for goal ID: {} and user ID: {}", goalId, user.getId());

        var goal = goalRepository.findByIdAndOwner(goalId, user)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND));

        var frequency = calculateFrequency(goal);
        var currentPeriod = calculateCurrentPeriod(goal, frequency);
        var lastReflection = reflectionRepository.findFirstByGoalAndUserOrderByPeriodEndDesc(goal, user);

        var reflectionCompleted = lastReflection
                .map(r -> r.getPeriodEnd().equals(currentPeriod[1]))
                .orElse(false);

        var today = LocalDate.now();
        var reflectionDue = !reflectionCompleted &&
                (today.isEqual(currentPeriod[1]) || today.isAfter(currentPeriod[1]));

        return ReflectionStatusResponse.builder()
                .goalId(goal.getId())
                .goalTitle(goal.getTitle())
                .frequency(frequency)
                .frequencyDays(frequency.getDays())
                .currentPeriodStart(currentPeriod[0])
                .currentPeriodEnd(currentPeriod[1])
                .reflectionDue(reflectionDue)
                .reflectionCompleted(reflectionCompleted)
                .lastReflectionDate(lastReflection.map(GoalReflection::getPeriodEnd).orElse(null))
                .totalReflections(reflectionRepository.countByGoalAndUser(goal, user))
                .averageRating(reflectionRepository.getAverageRating(goal, user))
                .build();
    }

    public List<PendingReflectionResponse> getPendingReflections(User user) {
        log.debug("Getting pending reflections for user ID: {}", user.getId());

        var activeGoals = goalRepository.findByOwnerAndGoalStatus(user, GoalStatus.ACTIVE);
        var pending = new ArrayList<PendingReflectionResponse>();
        var today = LocalDate.now();

        for (Goal goal : activeGoals) {
            var frequency = calculateFrequency(goal);
            var currentPeriod = calculateCurrentPeriod(goal, frequency);
            var lastReflection = reflectionRepository.findFirstByGoalAndUserOrderByPeriodEndDesc(goal, user);

            var reflectionCompleted = lastReflection
                    .map(r -> r.getPeriodEnd().equals(currentPeriod[1]))
                    .orElse(false);

            if (!reflectionCompleted && (today.isEqual(currentPeriod[1]) || today.isAfter(currentPeriod[1]))) {
                var daysOverdue = (int) ChronoUnit.DAYS.between(currentPeriod[1], today);
                pending.add(PendingReflectionResponse.builder()
                        .goalId(goal.getId())
                        .goalTitle(goal.getTitle())
                        .goalCategory(goal.getGoalCategory() != null ? goal.getGoalCategory().name() : null)
                        .frequency(frequency)
                        .periodStart(currentPeriod[0])
                        .periodEnd(currentPeriod[1])
                        .daysOverdue(Math.max(0, daysOverdue))
                        .build());
            }
        }

        return pending;
    }

    @Transactional
    public ReflectionResponse createReflection(Long goalId, ReflectionRequest request, User user) {
        log.info("Creating reflection for goal ID: {} by user ID: {}", goalId, user.getId());

        var goal = goalRepository.findByIdAndOwner(goalId, user)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND));

        var frequency = calculateFrequency(goal);
        var currentPeriod = calculateCurrentPeriod(goal, frequency);

        var existingReflection = reflectionRepository.findByGoalAndPeriod(
                goal, user, currentPeriod[0], currentPeriod[1]);

        if (existingReflection.isPresent()) {
            throw new BadRequestException(ErrorMessages.REFLECTION_ALREADY_EXISTS);
        }

        var reflection = GoalReflection.builder()
                .goal(goal)
                .user(user)
                .periodStart(currentPeriod[0])
                .periodEnd(currentPeriod[1])
                .rating(request.getRating())
                .wentWell(request.getWentWell())
                .challenges(request.getChallenges())
                .adjustments(request.getAdjustments())
                .moodNote(request.getMoodNote())
                .willContinue(request.getWillContinue())
                .motivationLevel(request.getMotivationLevel())
                .build();

        reflection = reflectionRepository.save(reflection);
        log.info("Created reflection ID: {} for goal ID: {}", reflection.getId(), goalId);

        return toResponse(reflection);
    }

    public Page<ReflectionResponse> getReflectionHistory(Long goalId, User user, Pageable pageable) {
        log.debug("Getting reflection history for goal ID: {} and user ID: {}", goalId, user.getId());

        var goal = goalRepository.findByIdAndOwner(goalId, user)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND));

        return reflectionRepository.findByGoalAndUserOrderByPeriodEndDesc(goal, user, pageable)
                .map(this::toResponse);
    }

    public ReflectionResponse getReflection(Long goalId, Long reflectionId, User user) {
        log.debug("Getting reflection ID: {} for goal ID: {}", reflectionId, goalId);

        var goal = goalRepository.findByIdAndOwner(goalId, user)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND));

        var reflection = reflectionRepository.findById(reflectionId)
                .filter(r -> r.getGoal().getId().equals(goal.getId()))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.REFLECTION_NOT_FOUND));

        return toResponse(reflection);
    }

    @Transactional
    public ReflectionResponse updateReflection(Long goalId, Long reflectionId, ReflectionRequest request, User user) {
        log.info("Updating reflection ID: {} for goal ID: {}", reflectionId, goalId);

        var goal = goalRepository.findByIdAndOwner(goalId, user)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND));

        var reflection = reflectionRepository.findById(reflectionId)
                .filter(r -> r.getGoal().getId().equals(goal.getId()))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.REFLECTION_NOT_FOUND));

        reflection.setRating(request.getRating());
        reflection.setWentWell(request.getWentWell());
        reflection.setChallenges(request.getChallenges());
        reflection.setAdjustments(request.getAdjustments());
        reflection.setMoodNote(request.getMoodNote());
        reflection.setWillContinue(request.getWillContinue());
        reflection.setMotivationLevel(request.getMotivationLevel());

        reflection = reflectionRepository.save(reflection);
        log.info("Updated reflection ID: {}", reflectionId);

        return toResponse(reflection);
    }

    private ReflectionFrequency calculateFrequency(Goal goal) {
        if (goal.getStartDate() == null || goal.getTargetDate() == null) {
            return ReflectionFrequency.WEEKLY;
        }

        var durationDays = ChronoUnit.DAYS.between(goal.getStartDate(), goal.getTargetDate());
        return ReflectionFrequency.fromGoalDuration(durationDays);
    }

    private LocalDate[] calculateCurrentPeriod(Goal goal, ReflectionFrequency frequency) {
        var startDate = goal.getStartDate() != null ? goal.getStartDate() : goal.getCreatedAt().toLocalDate();
        var today = LocalDate.now();
        var frequencyDays = frequency.getDays();

        var daysSinceStart = ChronoUnit.DAYS.between(startDate, today);
        var completedPeriods = daysSinceStart / frequencyDays;

        var periodStart = startDate.plusDays(completedPeriods * frequencyDays);
        var periodEnd = periodStart.plusDays(frequencyDays - 1);

        if (goal.getTargetDate() != null && periodEnd.isAfter(goal.getTargetDate())) {
            periodEnd = goal.getTargetDate();
        }

        return new LocalDate[]{periodStart, periodEnd};
    }

    private ReflectionResponse toResponse(GoalReflection reflection) {
        return ReflectionResponse.builder()
                .id(reflection.getId())
                .goalId(reflection.getGoal().getId())
                .goalTitle(reflection.getGoal().getTitle())
                .periodStart(reflection.getPeriodStart())
                .periodEnd(reflection.getPeriodEnd())
                .rating(reflection.getRating())
                .wentWell(reflection.getWentWell())
                .challenges(reflection.getChallenges())
                .adjustments(reflection.getAdjustments())
                .moodNote(reflection.getMoodNote())
                .willContinue(reflection.getWillContinue())
                .motivationLevel(reflection.getMotivationLevel())
                .createdAt(reflection.getCreatedAt())
                .build();
    }
}
