package com.relyon.metasmart.service;

import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.obstacle.dto.ObstacleEntryRequest;
import com.relyon.metasmart.entity.obstacle.dto.ObstacleEntryResponse;
import com.relyon.metasmart.entity.obstacle.dto.UpdateObstacleEntryRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.mapper.ObstacleEntryMapper;
import com.relyon.metasmart.repository.GoalRepository;
import com.relyon.metasmart.repository.ObstacleEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ObstacleService {

    private final ObstacleEntryRepository obstacleEntryRepository;
    private final GoalRepository goalRepository;
    private final ObstacleEntryMapper obstacleEntryMapper;

    @Transactional
    public ObstacleEntryResponse create(Long goalId, ObstacleEntryRequest request, User user) {
        log.debug("Creating obstacle entry for goal ID: {}", goalId);

        var goal = findGoalByIdAndOwner(goalId, user);
        var entry = obstacleEntryMapper.toEntity(request);
        entry.setGoal(goal);

        if (entry.getEntryDate() == null) {
            entry.setEntryDate(LocalDate.now());
        }

        var savedEntry = obstacleEntryRepository.save(entry);
        log.info("Obstacle entry created with ID: {} for goal ID: {}", savedEntry.getId(), goalId);

        return obstacleEntryMapper.toResponse(savedEntry);
    }

    @Transactional(readOnly = true)
    public Page<ObstacleEntryResponse> findByGoal(Long goalId, User user, Pageable pageable) {
        log.debug("Fetching obstacle entries for goal ID: {}", goalId);

        var goal = findGoalByIdAndOwner(goalId, user);
        return obstacleEntryRepository.findByGoalOrderByEntryDateDesc(goal, pageable)
                .map(obstacleEntryMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ObstacleEntryResponse> findByGoalAndDateRange(
            Long goalId, User user, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.debug("Fetching obstacle entries for goal ID: {} between {} and {}", goalId, startDate, endDate);

        var goal = findGoalByIdAndOwner(goalId, user);
        return obstacleEntryRepository.findByGoalAndEntryDateBetweenOrderByEntryDateDesc(
                goal, startDate, endDate, pageable)
                .map(obstacleEntryMapper::toResponse);
    }

    @Transactional
    public ObstacleEntryResponse update(Long goalId, Long entryId, UpdateObstacleEntryRequest request, User user) {
        log.debug("Updating obstacle entry ID: {} for goal ID: {}", entryId, goalId);

        var goal = findGoalByIdAndOwner(goalId, user);
        var entry = obstacleEntryRepository.findByIdAndGoal(entryId, goal)
                .orElseThrow(() -> {
                    log.warn("Obstacle entry not found with ID: {} for goal ID: {}", entryId, goalId);
                    return new ResourceNotFoundException(ErrorMessages.OBSTACLE_ENTRY_NOT_FOUND);
                });

        Optional.ofNullable(request.getObstacle()).ifPresent(entry::setObstacle);
        Optional.ofNullable(request.getSolution()).ifPresent(entry::setSolution);
        Optional.ofNullable(request.getResolved()).ifPresent(entry::setResolved);

        var savedEntry = obstacleEntryRepository.save(entry);
        log.info("Obstacle entry updated with ID: {} for goal ID: {}", savedEntry.getId(), goalId);

        return obstacleEntryMapper.toResponse(savedEntry);
    }

    @Transactional
    public void delete(Long goalId, Long entryId, User user) {
        log.debug("Deleting obstacle entry ID: {} from goal ID: {}", entryId, goalId);

        var goal = findGoalByIdAndOwner(goalId, user);
        var entry = obstacleEntryRepository.findByIdAndGoal(entryId, goal)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.OBSTACLE_ENTRY_NOT_FOUND));

        obstacleEntryRepository.delete(entry);
        log.info("Obstacle entry ID: {} deleted from goal ID: {}", entryId, goalId);
    }

    private Goal findGoalByIdAndOwner(Long goalId, User user) {
        return goalRepository.findByIdAndOwner(goalId, user)
                .orElseThrow(() -> {
                    log.warn("Goal not found with ID: {} for user ID: {}", goalId, user.getId());
                    return new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND);
                });
    }
}
