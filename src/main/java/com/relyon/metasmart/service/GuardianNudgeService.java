package com.relyon.metasmart.service;

import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.entity.guardian.GuardianNudge;
import com.relyon.metasmart.entity.guardian.GuardianPermission;
import com.relyon.metasmart.entity.guardian.GuardianStatus;
import com.relyon.metasmart.entity.guardian.dto.NudgeResponse;
import com.relyon.metasmart.entity.guardian.dto.ReactToNudgeRequest;
import com.relyon.metasmart.entity.guardian.dto.SendNudgeRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.AccessDeniedException;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.mapper.GuardianNudgeMapper;
import com.relyon.metasmart.repository.GoalGuardianRepository;
import com.relyon.metasmart.repository.GoalRepository;
import com.relyon.metasmart.repository.GuardianNudgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuardianNudgeService {

    private final GuardianNudgeRepository guardianNudgeRepository;
    private final GoalGuardianRepository goalGuardianRepository;
    private final GoalRepository goalRepository;
    private final GuardianNudgeMapper guardianNudgeMapper;

    @Transactional
    public NudgeResponse sendNudge(Long goalId, SendNudgeRequest request, User guardian) {
        log.debug("Guardian {} sending nudge to goal {}", guardian.getId(), goalId);

        var goalGuardian = goalGuardianRepository.findActiveGuardianship(goalId, guardian, GuardianStatus.ACTIVE)
                .orElseThrow(() -> {
                    log.warn("Active guardianship not found for goal {} and guardian {}", goalId, guardian.getId());
                    return new ResourceNotFoundException(ErrorMessages.GUARDIAN_NOT_FOUND);
                });

        if (!goalGuardian.getPermissions().contains(GuardianPermission.SEND_NUDGE)) {
            log.warn("Guardian {} does not have permission to send nudges for goal {}", guardian.getId(), goalId);
            throw new AccessDeniedException(ErrorMessages.GUARDIAN_PERMISSION_DENIED);
        }

        var nudge = guardianNudgeMapper.toEntity(request);
        nudge.setGoalGuardian(goalGuardian);

        var saved = guardianNudgeRepository.save(nudge);
        log.info("Nudge {} sent by guardian {} for goal {}", saved.getId(), guardian.getId(), goalId);

        return guardianNudgeMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<NudgeResponse> getNudgesForGoal(Long goalId, User owner, Pageable pageable) {
        log.debug("Getting nudges for goal {} by owner {}", goalId, owner.getId());

        goalRepository.findByIdAndOwner(goalId, owner)
                .orElseThrow(() -> {
                    log.warn("Goal not found with ID: {} for user ID: {}", goalId, owner.getId());
                    return new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND);
                });

        return guardianNudgeRepository.findByGoalId(goalId, pageable)
                .map(guardianNudgeMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public long countUnreadNudges(User owner) {
        log.debug("Counting unread nudges for owner {}", owner.getId());
        return guardianNudgeRepository.countUnreadByOwnerId(owner.getId());
    }

    @Transactional
    public NudgeResponse markAsRead(Long goalId, Long nudgeId, User owner) {
        log.debug("Marking nudge {} as read for goal {} by owner {}", nudgeId, goalId, owner.getId());

        goalRepository.findByIdAndOwner(goalId, owner)
                .orElseThrow(() -> {
                    log.warn("Goal not found with ID: {} for user ID: {}", goalId, owner.getId());
                    return new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND);
                });

        var nudge = guardianNudgeRepository.findById(nudgeId)
                .orElseThrow(() -> {
                    log.warn("Nudge not found with ID: {}", nudgeId);
                    return new ResourceNotFoundException(ErrorMessages.NUDGE_NOT_FOUND);
                });

        if (!nudge.getGoalGuardian().getGoal().getId().equals(goalId)) {
            log.warn("Nudge {} does not belong to goal {}", nudgeId, goalId);
            throw new AccessDeniedException(ErrorMessages.NUDGE_ACCESS_DENIED);
        }

        if (nudge.getReadAt() == null) {
            nudge.setReadAt(LocalDateTime.now());
            nudge = guardianNudgeRepository.save(nudge);
            log.info("Nudge {} marked as read", nudgeId);
        }

        return guardianNudgeMapper.toResponse(nudge);
    }

    @Transactional
    public NudgeResponse reactToNudge(Long goalId, Long nudgeId, ReactToNudgeRequest request, User owner) {
        log.debug("Reacting to nudge {} for goal {} by owner {}", nudgeId, goalId, owner.getId());

        goalRepository.findByIdAndOwner(goalId, owner)
                .orElseThrow(() -> {
                    log.warn("Goal not found with ID: {} for user ID: {}", goalId, owner.getId());
                    return new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND);
                });

        var nudge = guardianNudgeRepository.findById(nudgeId)
                .orElseThrow(() -> {
                    log.warn("Nudge not found with ID: {}", nudgeId);
                    return new ResourceNotFoundException(ErrorMessages.NUDGE_NOT_FOUND);
                });

        if (!nudge.getGoalGuardian().getGoal().getId().equals(goalId)) {
            log.warn("Nudge {} does not belong to goal {}", nudgeId, goalId);
            throw new AccessDeniedException(ErrorMessages.NUDGE_ACCESS_DENIED);
        }

        nudge.setReaction(request.getReaction());
        if (nudge.getReadAt() == null) {
            nudge.setReadAt(LocalDateTime.now());
        }

        var saved = guardianNudgeRepository.save(nudge);
        log.info("Nudge {} reacted with: {}", nudgeId, request.getReaction());

        return guardianNudgeMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<NudgeResponse> getSentNudges(User guardian, Pageable pageable) {
        log.debug("Getting sent nudges by guardian {}", guardian.getId());
        return guardianNudgeRepository.findSentByGuardian(guardian.getId(), pageable)
                .map(guardianNudgeMapper::toResponse);
    }
}
