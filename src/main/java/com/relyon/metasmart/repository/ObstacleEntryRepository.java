package com.relyon.metasmart.repository;

import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.obstacle.ObstacleEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ObstacleEntryRepository extends JpaRepository<ObstacleEntry, Long> {

    Page<ObstacleEntry> findByGoalOrderByEntryDateDesc(Goal goal, Pageable pageable);

    Page<ObstacleEntry> findByGoalAndEntryDateBetweenOrderByEntryDateDesc(
            Goal goal, LocalDate startDate, LocalDate endDate, Pageable pageable);

    Optional<ObstacleEntry> findByIdAndGoal(Long id, Goal goal);

    void deleteByGoal(Goal goal);

    long countByGoalAndResolvedFalse(Goal goal);
}
