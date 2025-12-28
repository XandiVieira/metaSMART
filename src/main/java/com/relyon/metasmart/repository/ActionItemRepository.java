package com.relyon.metasmart.repository;

import com.relyon.metasmart.entity.actionplan.ActionItem;
import com.relyon.metasmart.entity.goal.Goal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ActionItemRepository extends JpaRepository<ActionItem, Long> {

    List<ActionItem> findByGoalOrderByOrderIndexAscCreatedAtAsc(Goal goal);

    Optional<ActionItem> findByIdAndGoal(Long id, Goal goal);

    void deleteByGoal(Goal goal);

    long countByGoalAndCompletedTrue(Goal goal);

    long countByGoal(Goal goal);
}
