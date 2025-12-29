package com.relyon.metasmart.repository;

import com.relyon.metasmart.entity.goal.GoalCategory;
import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.goal.GoalStatus;
import com.relyon.metasmart.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    Page<Goal> findByOwner(User owner, Pageable pageable);

    Page<Goal> findByOwnerAndGoalStatus(User owner, GoalStatus goalStatus, Pageable pageable);

    Page<Goal> findByOwnerAndGoalCategory(User owner, GoalCategory goalCategory, Pageable pageable);

    Optional<Goal> findByIdAndOwner(Long id, User owner);

    java.util.List<Goal> findByOwnerAndGoalStatus(User owner, GoalStatus goalStatus);
}
