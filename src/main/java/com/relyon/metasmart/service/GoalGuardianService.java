package com.relyon.metasmart.service;

import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.guardian.GoalGuardian;
import com.relyon.metasmart.entity.guardian.GuardianPermission;
import com.relyon.metasmart.entity.guardian.GuardianStatus;
import com.relyon.metasmart.entity.guardian.dto.GoalGuardianResponse;
import com.relyon.metasmart.entity.guardian.dto.GuardedGoalResponse;
import com.relyon.metasmart.entity.guardian.dto.InviteGuardianRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.AccessDeniedException;
import com.relyon.metasmart.exception.BadRequestException;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.mapper.GoalGuardianMapper;
import com.relyon.metasmart.repository.ActionItemRepository;
import com.relyon.metasmart.repository.GoalGuardianRepository;
import com.relyon.metasmart.repository.GoalRepository;
import com.relyon.metasmart.repository.ObstacleEntryRepository;
import com.relyon.metasmart.repository.ProgressEntryRepository;
import com.relyon.metasmart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoalGuardianService {

    private final GoalGuardianRepository goalGuardianRepository;
    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final ProgressEntryRepository progressEntryRepository;
    private final ActionItemRepository actionItemRepository;
    private final ObstacleEntryRepository obstacleEntryRepository;
    private final GoalGuardianMapper goalGuardianMapper;

    @Transactional
    public GoalGuardianResponse inviteGuardian(Long goalId, InviteGuardianRequest request, User owner) {
        log.debug("Inviting guardian {} for goal ID: {}", request.getGuardianEmail(), goalId);

        var goal = goalRepository.findByIdAndOwner(goalId, owner)
                .orElseThrow(() -> {
                    log.warn("Goal not found with ID: {} for user ID: {}", goalId, owner.getId());
                    return new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND);
                });

        var guardian = userRepository.findByEmail(request.getGuardianEmail())
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", request.getGuardianEmail());
                    return new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND);
                });

        if (guardian.getId().equals(owner.getId())) {
            log.warn("User {} attempted to be their own guardian", owner.getId());
            throw new BadRequestException(ErrorMessages.CANNOT_BE_OWN_GUARDIAN);
        }

        if (goalGuardianRepository.existsByGoalAndGuardianAndStatusNot(goal, guardian, GuardianStatus.REVOKED)) {
            log.warn("Guardian {} already exists for goal {}", guardian.getId(), goalId);
            throw new BadRequestException(ErrorMessages.GUARDIAN_ALREADY_EXISTS);
        }

        var goalGuardian = GoalGuardian.builder()
                .goal(goal)
                .guardian(guardian)
                .owner(owner)
                .status(GuardianStatus.PENDING)
                .permissions(request.getPermissions())
                .inviteMessage(request.getInviteMessage())
                .build();

        var saved = goalGuardianRepository.save(goalGuardian);
        log.info("Guardian invitation created with ID: {} for goal ID: {}", saved.getId(), goalId);

        return goalGuardianMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<GoalGuardianResponse> getGuardiansForGoal(Long goalId, User owner) {
        log.debug("Getting guardians for goal ID: {} by owner ID: {}", goalId, owner.getId());

        var goal = goalRepository.findByIdAndOwner(goalId, owner)
                .orElseThrow(() -> {
                    log.warn("Goal not found with ID: {} for user ID: {}", goalId, owner.getId());
                    return new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND);
                });

        var guardians = goalGuardianRepository.findByGoalAndStatusNot(goal, GuardianStatus.REVOKED);
        return goalGuardianMapper.toResponseList(guardians);
    }

    @Transactional
    public void removeGuardian(Long goalId, Long guardianshipId, User owner) {
        log.debug("Removing guardian {} from goal ID: {}", guardianshipId, goalId);

        var goal = goalRepository.findByIdAndOwner(goalId, owner)
                .orElseThrow(() -> {
                    log.warn("Goal not found with ID: {} for user ID: {}", goalId, owner.getId());
                    return new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND);
                });

        var goalGuardian = goalGuardianRepository.findById(guardianshipId)
                .orElseThrow(() -> {
                    log.warn("Guardian relationship not found with ID: {}", guardianshipId);
                    return new ResourceNotFoundException(ErrorMessages.GUARDIAN_NOT_FOUND);
                });

        if (!goalGuardian.getGoal().getId().equals(goal.getId())) {
            log.warn("Guardian {} does not belong to goal {}", guardianshipId, goalId);
            throw new AccessDeniedException(ErrorMessages.GUARDIAN_PERMISSION_DENIED);
        }

        goalGuardian.setStatus(GuardianStatus.REVOKED);
        goalGuardian.setRevokedAt(LocalDateTime.now());
        goalGuardianRepository.save(goalGuardian);
        log.info("Guardian {} revoked from goal ID: {}", guardianshipId, goalId);
    }

    @Transactional(readOnly = true)
    public Page<GoalGuardianResponse> getPendingInvitations(User guardian, Pageable pageable) {
        log.debug("Getting pending invitations for user ID: {}", guardian.getId());
        return goalGuardianRepository.findByGuardianAndStatus(guardian, GuardianStatus.PENDING, pageable)
                .map(goalGuardianMapper::toResponse);
    }

    @Transactional
    public GoalGuardianResponse acceptInvitation(Long invitationId, User guardian) {
        log.debug("Accepting invitation ID: {} by user ID: {}", invitationId, guardian.getId());

        var goalGuardian = goalGuardianRepository.findById(invitationId)
                .orElseThrow(() -> {
                    log.warn("Invitation not found with ID: {}", invitationId);
                    return new ResourceNotFoundException(ErrorMessages.GUARDIAN_INVITATION_NOT_FOUND);
                });

        if (!goalGuardian.getGuardian().getId().equals(guardian.getId())) {
            log.warn("User {} is not the invited guardian for invitation {}", guardian.getId(), invitationId);
            throw new AccessDeniedException(ErrorMessages.GUARDIAN_PERMISSION_DENIED);
        }

        if (goalGuardian.getStatus() != GuardianStatus.PENDING) {
            log.warn("Invitation {} is not pending, current status: {}", invitationId, goalGuardian.getStatus());
            throw new BadRequestException(ErrorMessages.GUARDIAN_INVITATION_NOT_FOUND);
        }

        goalGuardian.setStatus(GuardianStatus.ACTIVE);
        goalGuardian.setAcceptedAt(LocalDateTime.now());
        var saved = goalGuardianRepository.save(goalGuardian);
        log.info("Invitation {} accepted by user ID: {}", invitationId, guardian.getId());

        return goalGuardianMapper.toResponse(saved);
    }

    @Transactional
    public GoalGuardianResponse declineInvitation(Long invitationId, User guardian) {
        log.debug("Declining invitation ID: {} by user ID: {}", invitationId, guardian.getId());

        var goalGuardian = goalGuardianRepository.findById(invitationId)
                .orElseThrow(() -> {
                    log.warn("Invitation not found with ID: {}", invitationId);
                    return new ResourceNotFoundException(ErrorMessages.GUARDIAN_INVITATION_NOT_FOUND);
                });

        if (!goalGuardian.getGuardian().getId().equals(guardian.getId())) {
            log.warn("User {} is not the invited guardian for invitation {}", guardian.getId(), invitationId);
            throw new AccessDeniedException(ErrorMessages.GUARDIAN_PERMISSION_DENIED);
        }

        if (goalGuardian.getStatus() != GuardianStatus.PENDING) {
            log.warn("Invitation {} is not pending, current status: {}", invitationId, goalGuardian.getStatus());
            throw new BadRequestException(ErrorMessages.GUARDIAN_INVITATION_NOT_FOUND);
        }

        goalGuardian.setStatus(GuardianStatus.DECLINED);
        goalGuardian.setDeclinedAt(LocalDateTime.now());
        var saved = goalGuardianRepository.save(goalGuardian);
        log.info("Invitation {} declined by user ID: {}", invitationId, guardian.getId());

        return goalGuardianMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<GoalGuardianResponse> getGuardedGoals(User guardian, Pageable pageable) {
        log.debug("Getting guarded goals for user ID: {}", guardian.getId());
        return goalGuardianRepository.findActiveGuardianships(guardian, GuardianStatus.ACTIVE, pageable)
                .map(goalGuardianMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public GuardedGoalResponse getGuardedGoalDetails(Long goalId, User guardian) {
        log.debug("Getting guarded goal details for goal ID: {} by guardian ID: {}", goalId, guardian.getId());

        var goalGuardian = goalGuardianRepository.findActiveGuardianship(goalId, guardian, GuardianStatus.ACTIVE)
                .orElseThrow(() -> {
                    log.warn("Active guardianship not found for goal {} and guardian {}", goalId, guardian.getId());
                    return new ResourceNotFoundException(ErrorMessages.GUARDIAN_NOT_FOUND);
                });

        var goal = goalGuardian.getGoal();
        var permissions = goalGuardian.getPermissions();

        var response = GuardedGoalResponse.builder()
                .goalId(goal.getId())
                .title(goal.getTitle())
                .description(goal.getDescription())
                .category(goal.getGoalCategory())
                .status(goal.getGoalStatus())
                .ownerName(goalGuardian.getOwner().getName())
                .goalGuardianId(goalGuardian.getId())
                .permissions(permissions)
                .startDate(goal.getStartDate())
                .targetDate(goal.getTargetDate())
                .build();

        if (permissions.contains(GuardianPermission.VIEW_PROGRESS)) {
            response.setCurrentProgress(goal.getCurrentProgress());
            response.setTargetValue(goal.getTargetValue());
            response.setUnit(goal.getUnit());
            response.setProgressPercentage(calculateProgressPercentage(goal));
            response.setLastProgressAt(progressEntryRepository.findTopByGoalOrderByCreatedAtDesc(goal)
                    .map(entry -> entry.getCreatedAt())
                    .orElse(null));
        }

        if (permissions.contains(GuardianPermission.VIEW_STREAK)) {
            var streaks = calculateStreaks(goal);
            response.setCurrentStreak(streaks[0]);
            response.setLongestStreak(streaks[1]);
        }

        if (permissions.contains(GuardianPermission.VIEW_ACTION_PLAN)) {
            var totalItems = actionItemRepository.countByGoal(goal);
            var completedItems = actionItemRepository.countByGoalAndCompletedTrue(goal);
            response.setTotalActionsCount((int) totalItems);
            response.setCompletedActionsCount((int) completedItems);
        }

        if (permissions.contains(GuardianPermission.VIEW_OBSTACLES)) {
            response.setUnresolvedObstaclesCount((int) obstacleEntryRepository.countByGoalAndResolvedFalse(goal));
        }

        return response;
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
        var today = java.time.LocalDate.now();

        if (dates.get(0).equals(today) || dates.get(0).equals(today.minusDays(1))) {
            currentStreak = 1;
        }

        for (var i = 0; i < dates.size() - 1; i++) {
            var current = dates.get(i);
            var next = dates.get(i + 1);

            if (current.minusDays(1).equals(next)) {
                streak++;
                if (i == 0 || (dates.get(0).equals(today) || dates.get(0).equals(today.minusDays(1)))) {
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
}
