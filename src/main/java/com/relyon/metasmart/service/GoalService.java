package com.relyon.metasmart.service;

import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.entity.goal.GoalCategory;
import com.relyon.metasmart.entity.goal.GoalStatus;
import com.relyon.metasmart.entity.goal.dto.GoalRequest;
import com.relyon.metasmart.entity.goal.dto.GoalResponse;
import com.relyon.metasmart.entity.goal.dto.UpdateGoalRequest;
import com.relyon.metasmart.entity.progress.Milestone;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.mapper.GoalMapper;
import com.relyon.metasmart.repository.GoalRepository;
import com.relyon.metasmart.repository.MilestoneRepository;
import com.relyon.metasmart.repository.ProgressEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final MilestoneRepository milestoneRepository;
    private final ProgressEntryRepository progressEntryRepository;
    private final GoalMapper goalMapper;

    @Transactional
    public GoalResponse create(GoalRequest request, User owner) {
        log.debug("Creating goal for user ID: {}", owner.getId());

        var goal = goalMapper.toEntity(request);
        goal.setOwner(owner);

        var savedGoal = goalRepository.save(goal);
        log.info("Goal created with ID: {} for user ID: {}", savedGoal.getId(), owner.getId());

        createDefaultMilestones(savedGoal);

        return goalMapper.toResponse(savedGoal);
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
                .map(goalMapper::toResponse)
                .orElseThrow(() -> {
                    log.warn("Goal not found with ID: {} for user ID: {}", id, owner.getId());
                    return new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND);
                });
    }

    @Transactional(readOnly = true)
    public Page<GoalResponse> findAll(User owner, Pageable pageable) {
        log.debug("Finding all goals for user ID: {}", owner.getId());
        return goalRepository.findByOwner(owner, pageable)
                .map(goalMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<GoalResponse> findByStatus(User owner, GoalStatus goalStatus, Pageable pageable) {
        log.debug("Finding goals by status: {} for user ID: {}", goalStatus, owner.getId());
        return goalRepository.findByOwnerAndGoalStatus(owner, goalStatus, pageable)
                .map(goalMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<GoalResponse> findByCategory(User owner, GoalCategory goalCategory, Pageable pageable) {
        log.debug("Finding goals by category: {} for user ID: {}", goalCategory, owner.getId());
        return goalRepository.findByOwnerAndGoalCategory(owner, goalCategory, pageable)
                .map(goalMapper::toResponse);
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
        return goalMapper.toResponse(savedGoal);
    }

    @Transactional
    public void delete(Long id, User owner) {
        log.debug("Deleting goal ID: {} for user ID: {}", id, owner.getId());

        var goal = goalRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> {
                    log.warn("Goal not found with ID: {} for user ID: {}", id, owner.getId());
                    return new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND);
                });

        progressEntryRepository.deleteByGoal(goal);
        milestoneRepository.deleteByGoal(goal);
        goalRepository.delete(goal);
        log.info("Goal deleted with ID: {}", id);
    }
}
