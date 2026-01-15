package com.relyon.metasmart.repository;

import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.progress.Milestone;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MilestoneRepository extends JpaRepository<Milestone, Long> {

    List<Milestone> findByGoalOrderByPercentageAsc(Goal goal);

    List<Milestone> findByGoalAndAchievedFalseOrderByPercentageAsc(Goal goal);

    Optional<Milestone> findByIdAndGoal(Long id, Goal goal);

    void deleteByGoal(Goal goal);

    boolean existsByGoalAndPercentage(Goal goal, Integer percentage);
}
